import os
from ldplayer_manager import LDPlayerManager

def test_full_automation():
    print("=== Testando Automa??o Completa do LDPlayer ===")
    try:
        ld = LDPlayerManager(emulator_name="LDPlayer")
        print(f"LDPlayer console encontrado em: {ld.ldconsole}")
        print(f"ADB encontrado em: {ld.adb}")
        
        # Testando comando de console b?sico
        status_info = ld.execute_console("list2")
        print(f"Inst?ncias ativas:\n{status_info}")
        
        # Testando captura de tela nativa no emulador se ele j? estiver ativo
        if "1280" in status_info:
            print("Emulador ativo detectado. Testando captura de tela imediata...")
            ss_path = os.path.abspath("test_screenshot.png")
            print(ld.take_screenshot(ss_path))
        else:
            print("Emulador inativo. Para testar o auto_deploy completo, execute com um APK v?lido.")
            
    except FileNotFoundError as e:
        print(f"Aviso: {e}")
    except Exception as e:
        print(f"Erro geral: {e}")

if __name__ == "__main__":
    test_full_automation()
