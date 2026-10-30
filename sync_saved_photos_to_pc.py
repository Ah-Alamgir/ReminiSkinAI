import os
import subprocess
import time
import shutil

ADB = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
REMOTE_DIR = "/sdcard/Pictures/ReminiSkinAI"
PC_DIR_1 = r"C:\Users\abuha\Pictures\ReminiSkinAI"
PC_DIR_2 = r"C:\Users\abuha\Pictures\Screenshots\aiapp\Saved_From_App"

os.makedirs(PC_DIR_1, exist_ok=True)
os.makedirs(PC_DIR_2, exist_ok=True)

def sync_once():
    try:
        res = subprocess.run(
            [ADB, "shell", f"ls -1 {REMOTE_DIR} 2>/dev/null"],
            capture_output=True,
            text=True,
            timeout=5
        )
        if res.returncode != 0:
            return
        files = [line.strip() for line in res.stdout.splitlines() if line.strip().endswith((".jpg", ".jpeg", ".png", ".webp"))]
        for fname in files:
            dest1 = os.path.join(PC_DIR_1, fname)
            dest2 = os.path.join(PC_DIR_2, fname)
            if not os.path.exists(dest1) or os.path.getsize(dest1) == 0:
                subprocess.run([ADB, "pull", f"{REMOTE_DIR}/{fname}", dest1], capture_output=True, timeout=10)
            if os.path.exists(dest1) and not os.path.exists(dest2):
                shutil.copy2(dest1, dest2)
    except Exception:
        pass

if __name__ == "__main__":
    print(f"Syncing saved photos from {REMOTE_DIR} to:\n  1) {PC_DIR_1}\n  2) {PC_DIR_2}")
    while True:
        sync_once()
        time.sleep(2.0)
