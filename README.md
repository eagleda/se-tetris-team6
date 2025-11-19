### SE 2025 Team Project Tetris - Team 6

## 배포 패키지 제작 가이드 (Windows)

팀 내에서 Windows 환경을 사용해 아이콘이 포함된 실행 파일을 손쉽게 만들 수 있도록 Gradle 태스크를 추가했습니다. `JAVA_HOME`이 JDK 14 이상(권장 JDK 17)을 가리키고 있어야 하며, 해당 JDK에 `jpackage` 도구가 포함되어 있어야 합니다.

### 준비 사항
- Windows 10 이상 PC
- `JAVA_HOME` 환경변수가 JDK 14+ (예: `C:\Program Files\Java\jdk-17`)를 가리키도록 설정
- 저장소 루트에서 `gradlew.bat` 실행이 가능해야 함

### 패키징 절차
1. Windows에서 프로젝트 루트(`se-tetris-team6`)로 이동합니다.
2. 명령 프롬프트 또는 PowerShell에서 다음을 실행합니다.
   ```powershell
   .\gradlew.bat :app:packageWinZip
   ```
3. 실행이 완료되면 `app/build/package/windows/Team6Tetris-windows.zip` 파일이 생성됩니다.  
   압축을 풀면 `Team6Tetris/Team6Tetris.exe` 실행 파일과 필요한 라이브러리가 모두 포함된 폴더가 생성됩니다.
4. `Team6Tetris.exe`를 더블클릭하면 아이콘(`assets/tetris.ico`)이 적용된 상태로 게임이 실행됩니다.  
   (JAVA_HOME이 설정되지 않은 PC에서는 실행되지 않을 수 있습니다.)

### 참고 사항
- `packageWinAppImage` 태스크는 `Team6Tetris.exe`가 포함된 폴더만 생성합니다. 배포를 위해서는 `packageWinZip` 태스크가 자동으로 ZIP까지 만들어 줍니다.
- 다른 OS 타겟이 필요한 경우, 동일한 구조를 참고해 macOS용 `jpackage` 옵션을 추가하면 됩니다.

## 배포 패키지 제작 가이드 (macOS)

macOS에서는 `jpackage`가 `.icns` 아이콘 파일을 요구합니다. `assets/tetris.icns`가 없으면 아래 예시처럼 한 번만 생성해 주세요(아이콘 원본은 PNG 권장).

```bash
mkdir -p build/iconset
sips -z 16 16     assets/tetris.png --out build/iconset/icon_16x16.png
sips -z 32 32     assets/tetris.png --out build/iconset/icon_16x16@2x.png
sips -z 32 32     assets/tetris.png --out build/iconset/icon_32x32.png
sips -z 64 64     assets/tetris.png --out build/iconset/icon_32x32@2x.png
sips -z 128 128   assets/tetris.png --out build/iconset/icon_128x128.png
sips -z 256 256   assets/tetris.png --out build/iconset/icon_128x128@2x.png
sips -z 256 256   assets/tetris.png --out build/iconset/icon_256x256.png
sips -z 512 512   assets/tetris.png --out build/iconset/icon_256x256@2x.png
sips -z 512 512   assets/tetris.png --out build/iconset/icon_512x512.png
sips -z 1024 1024 assets/tetris.png --out build/iconset/icon_512x512@2x.png
iconutil -c icns build/iconset
mv build/iconset.icns assets/tetris.icns
```

### 준비 사항
- macOS 12 이상
- `JAVA_HOME`이 JDK 14+ (예: `/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home`)를 가리키도록 설정
- `assets/tetris.icns` 아이콘 파일

### 패키징 절차
1. 프로젝트 루트에서 다음을 실행합니다.
   ```bash
   ./gradlew :app:packageMacDmg
   ```
2. 빌드가 완료되면 결과물은 다음 위치에 생성됩니다.
   - 앱 번들: `app/build/package/macos/Team6Tetris.app`
   - 배포용 DMG: `app/build/package/macos/Team6Tetris.dmg`
3. DMG를 더블클릭하면 Finder에서 `Team6Tetris.app`을 애플리케이션 폴더 등으로 드래그해 설치할 수 있습니다. 처음 실행 시 Gatekeeper 경고가 뜨면 `시스템 설정 > 개인정보 보호 및 보안`에서 “허용” 후 다시 실행하면 됩니다.

### 참고 사항
- DMG 생성 시 내부적으로 앞서 만들어 둔 `.app` 번들을 다시 사용하므로, `packageMacAppImage`만 실행하면 `.app`만 생성되고 DMG는 만들지 않습니다.
- macOS 패키지는 JRE를 포함하지 않으므로 대상 PC에도 JDK/JRE가 설치되어 있어야 합니다. 필요하면 `--runtime-image` 옵션을 추가해 JRE를 번들링할 수 있습니다.
