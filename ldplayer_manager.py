import os
import subprocess
import time
import winreg

class LDPlayerManager:
    def __init__(self, emulator_name="LDPlayer"):
        self.emulator_name = emulator_name
        self.ld_path = self._find_ldplayer_path()
        if not self.ld_path:
            raise FileNotFoundError("LDPlayer n?o foi encontrado no registro ou caminhos padr?o.")
        
        self.ldconsole = os.path.join(self.ld_path, "ldconsole.exe")
        self.adb = os.path.join(self.ld_path, "adb.exe")

    def _find_ldplayer_path(self):
        # Tenta buscar no registro do Windows para o LDPlayer 9 primeiro
        try:
            with winreg.OpenKey(winreg.HKEY_CURRENT_USER, r"SOFTWARE\XuanZhi\LDPlayer9") as key:
                path, _ = winreg.QueryValueEx(key, "InstallDir")
                if os.path.exists(os.path.join(path, "ldconsole.exe")):
                    return path
        except:
            pass

        # Tenta buscar no registro do Windows para o LDPlayer 4 / alternativos
        try:
            with winreg.OpenKey(winreg.HKEY_CURRENT_USER, r"SOFTWARE\XuanZhi\LDPlayer") as key:
                path, _ = winreg.QueryValueEx(key, "InstallDir")
                if os.path.exists(os.path.join(path, "ldconsole.exe")):
                    return path
        except:
            pass
        
        # Caminhos padr?o de fallback
        default_paths = [
            r"C:\LDPlayer\LDPlayer9",
            r"D:\LDPlayer\LDPlayer9",
            r"C:\XuanZhi\LDPlayer",
            r"C:\LDPlayer\LDPlayer4"
        ]
        for path in default_paths:
            if os.path.exists(os.path.join(path, "ldconsole.exe")):
                return path
        return None

    def execute_console(self, command, *args):
        """Executa um comando no ldconsole.exe"""
        cmd = [self.ldconsole, command, "--name", self.emulator_name] + list(args)
        try:
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            return result.stdout.strip()
        except subprocess.CalledProcessError as e:
            return f"Erro: {e.stderr}"

    def launch_emulator(self):
        """Inicia o emulador em segundo plano (se n?o estiver aberto)"""
        print(f"> Iniciando emulador {self.emulator_name}...")
        self.execute_console("launch")
        
        # Aguarda o emulador ficar pronto e acess?vel via ADB
        print("> Aguardando inicializa??o do sistema Android no emulador...")
        for attempt in range(30):
            status = self.adb_command("shell", "getprop", "sys.boot_completed")
            if status == "1":
                print("[+] Emulador totalmente iniciado e pronto via ADB.")
                return "Emulador iniciado com sucesso."
            time.sleep(2)
            
        print("[!] Aviso: Timeout aguardando boot_completed. Proseguindo de qualquer forma.")
        return "Emulador iniciado (boot_completed timeout)."

    def quit_emulator(self):
        """Fecha o emulador"""
        self.execute_console("quit")
        return "Emulador encerrado."

    def install_apk(self, apk_path):
        """Instala um arquivo .apk no emulador"""
        if not os.path.exists(apk_path):
            return f"Erro: APK n?o encontrado em {apk_path}"
        print(f"> Instalando {os.path.basename(apk_path)}...")
        self.execute_console("installapp", "--filename", apk_path)
        
        # Aguarda a instala??o ser registrada
        time.sleep(3)
        return "Aplicativo instalado com sucesso."

    def run_app(self, package_name):
        """Abre um aplicativo j? instalado pelo nome do pacote (ex: com.whatsapp)"""
        self.execute_console("runapp", "--packagename", package_name)
        return f"Aplicativo {package_name} iniciado."
        
    def adb_command(self, *args):
        """Executa comandos ADB diretos (ex: cliques na tela, swipes)"""
        cmd = [self.adb, "-s", "emulator-5554"] + list(args) # emulator-5554 ? a porta padr?o da 1? inst?ncia
        try:
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
            return result.stdout.strip()
        except:
            return "Erro ao executar comando ADB."

    def take_screenshot(self, local_path):
        """Tira uma captura de tela do emulador e salva localmente"""
        print(f"> Capturando tela do emulador...")
        temp_path = "/sdcard/autoscreenshot.png"
        self.adb_command("shell", "screencap", "-p", temp_path)
        time.sleep(1)
        
        # Garante que o diret?rio de destino local exista
        dest_dir = os.path.dirname(os.path.abspath(local_path))
        if dest_dir:
            os.makedirs(dest_dir, exist_ok=True)
            
        self.adb_command("pull", temp_path, local_path)
        self.adb_command("shell", "rm", temp_path)
        
        if os.path.exists(local_path) and os.path.getsize(local_path) > 0:
            return f"Captura de tela salva com sucesso em: {local_path}"
        else:
            return "Erro: Falha ao capturar a tela ou extrair o arquivo."

    def auto_deploy(self, apk_path, package_name, screenshot_path):
        """Executa todo o fluxo de forma 100% aut?noma e inteligente"""
        print("\n=================================================")
        print(" AG-TOOLKIT - AUTONOMOUS LDPLAYER DEPLOYMENT")
        print("=================================================")
        
        # 1. Inicia
        print(self.launch_emulator())
        
        # 2. Instala
        print(self.install_apk(apk_path))
        
        # 3. Executa
        print(self.run_app(package_name))
        
        # 4. Aguarda interface renderizar
        print("> Aguardando renderiza??o da interface do aplicativo (5s)...")
        time.sleep(5)
        
        # 5. Tira Screenshot
        result = self.take_screenshot(screenshot_path)
        print(result)
        return result

# Exemplo de uso para o agente:
if __name__ == "__main__":
    try:
        ld = LDPlayerManager(emulator_name="LDPlayer") # Nome padr?o
        # Exemplo: ld.auto_deploy("caminho/do/app.apk", "com.strategy.note", "capturas/home.png")
    except Exception as e:
        print(e)
