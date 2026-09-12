# mall-android

Android 商城 App（Kotlin + Jetpack Compose）

## 环境要求

- **Android Studio** — 最新版
- **JDK** — 17 或 21（LTS 版本），项目已配置 `auto-detect`，Gradle 会自动找本机 JDK
- **Android SDK** — 本机已安装即可，`local.properties` 会自动生成

## 构建

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

## 配置

- Gradle 镜像使用腾讯云 CDN（国内加速）
- Maven 仓库优先走阿里云镜像
- JDK 通过 `auto-detect` 自动发现，不写死路径

## 项目结构

```
mall-android/
├── app/
│   ├── src/main/java/.../MainActivity.kt   # 主入口
│   └── src/main/res/                       # 资源文件
├── build.gradle.kts                         # 根构建脚本
├── settings.gradle.kts                      # 仓库配置
├── gradle.properties                        # Gradle 全局配置
└── gradle/libs.versions.toml               # 版本目录
```
