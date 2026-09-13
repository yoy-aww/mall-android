# mall-android

Android 端商城客户端：基于 **Kotlin + Jetpack Compose** 实现浏览、搜索、购物车、登录注册、下单支付、订单/地址/售后/消息等完整购物流程。

| 项 | 说明 |
|---|---|
| 包名 / applicationId | `com.example.mall_android` |
| 最低 / 目标 SDK | minSdk 24 · targetSdk / compileSdk 36 |
| 版本 | versionCode 1 · versionName 1.0 |
| UI | Jetpack Compose + Material 3 |
| 网络 | OkHttp 4.12 + Gson 2.11（自封装 `ApiClient`，非 Retrofit） |
| 图片 | Coil Compose 2.7 |
| 导航 | Navigation Compose 2.8 |
| 架构 | 单 Activity、单模块；无 ViewModel / DI / Repository 分层 |

---

## 功能一览

- **首页**：Banner 轮播、分类入口、热销商品
- **搜索**：关键词搜索商品
- **分类**：按分类浏览商品列表
- **商品详情**：价格/库存/评价、加入购物车
- **购物车**：本地加减删、去结算（进程内内存，杀进程清空）
- **登录 / 注册**：Token 持久化到 SharedPreferences
- **个人中心**：资料编辑、改密、退出登录
- **结算 / 支付**：选地址、订单预览、创建订单、模拟支付
- **订单**：列表筛选、详情、取消、确认收货、申请售后入口
- **地址**：收货地址 CRUD
- **售后 / 消息**：售后列表、通知已读

典型流转：

```
首页 → 分类/商品详情 → 加购 → 购物车 → 结算 → 支付
我的 → 登录/注册 → 订单 / 地址 / 售后 / 消息 / 编辑资料
```

---

## 技术架构

### 总体结构

```
MainActivity
  └─ MallTheme
       └─ MallApp（Scaffold + 底部 Tab + NavHost）
            ├─ AuthStore      SharedPreferences 存 token / user
            ├─ ApiClient      OkHttp 调后端 /api
            └─ CartManager    内存购物车单例
```

依赖在 `MainActivity.onCreate` 中手工创建并向下传递，页面内用 `remember` / `mutableStateOf` + `LaunchedEffect` 管理状态与请求，**未使用** ViewModel、Flow、Room、DataStore。

### 数据流

```
AuthStore (auth_prefs) ──Bearer──► ApiClient ──OkHttp──► 后端 :3456/api
                                      ▲
Screens (Compose 状态) ◄── Result<T> ─┘
         ▲
CartManager（内存 List<CartItem>）
```

### 后端约定

- Base URL（模拟器访问本机）：`http://10.0.2.2:3456/api`  
  定义于 `ApiClient.BASE_URL`
- 响应包装：`{ success, data, error }`
- 鉴权头：`Authorization: Bearer {token}`
- 超时：连接 / 读取各 30 秒

真机调试时请改为电脑局域网 IP（如 `http://192.168.x.x:3456/api`）。

---

## 目录结构

```
mall-android/
├── app/
│   ├── build.gradle.kts                 # 模块构建、依赖、签名
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/mall_android/
│       │   ├── MainActivity.kt          # 入口 + MallApp 导航
│       │   ├── ApiClient.kt             # 全部 HTTP API
│       │   ├── AuthStore.kt             # 登录态持久化
│       │   ├── CartManager.kt           # 本地购物车
│       │   ├── model/Models.kt          # 数据模型
│       │   └── ui/
│       │       ├── components/Common.kt # 公共组件
│       │       ├── screens/             # 业务页面
│       │       └── theme/               # 主题 / 颜色 / 字体
│       └── res/                         # 图标、字符串、主题
├── gradle/libs.versions.toml            # 版本目录
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

单模块工程，仅 `:app`，无自定义 `Application` 类。

---

## 页面与路由

底部四个 Tab：`home` / `search` / `cart` / `profile`，起始页为首页。

| 路由 | 页面 | 说明 |
|---|---|---|
| `home` | HomeScreen | 首页 |
| `search` | SearchScreen | 搜索 |
| `cart` | CartScreen | 购物车 |
| `profile` | ProfileScreen | 我的 |
| `product/{id}` | ProductDetailScreen | 商品详情 |
| `category/{id}` | CategoryScreen | 分类商品 |
| `auth` | AuthScreen | 登录注册 |
| `checkout/{addressId}` | CheckoutScreen | 结算 |
| `payment/{orderId}` | PaymentScreen | 支付 |
| `orders` | OrdersScreen | 订单列表 |
| `order/{id}` | OrderDetailScreen | 订单详情 |
| `addresses` | AddressesScreen | 地址列表 |
| `address-edit/{id}` | AddressEditScreen | 编辑地址 |
| `aftersales` | AfterSalesScreen | 售后 |
| `notifications` | NotificationsScreen | 消息 |
| `profile-edit` | ProfileEditScreen | 编辑资料 / 改密 |

公共 UI 组件（`ui/components/Common.kt`）：`ProductCard`、`ProductGrid`、`PriceText`、`StatusChip`、`LoadingView`、`ErrorView`、`EmptyView` 等。

订单状态文案映射（`StatusChip`）：`pending` / `paid` / `shipped` / `delivered` / `completed` / `cancelled` / `refund`。

---

## 网络接口（ApiClient）

| 领域 | 方法示例 | 路径 |
|---|---|---|
| Banner | `getBanners` | `GET /banners` |
| 分类 | `getCategories` | `GET /categories` |
| 商品 | `getProducts` / `getProduct` / `searchProducts` / `getPopular` 等 | `/products...` |
| 认证 | `login` / `register` / `getMe` / `updateMe` / `changePassword` | `/auth/...` |
| 评价 | `getReviews` / `getReviewStats` / `createReview` | `/reviews...` |
| 订单 | `previewOrder` / `createOrder` / `payOrder` / `cancelOrder` / `confirmDelivery` 等 | `/orders...` |
| 地址 | `getAddresses` / `createAddress` / `updateAddress` / `deleteAddress` | `/addresses...` |
| 售后 | `getAfterSales` / `createAfterSale` | `/aftersales` |
| 消息 | `getNotifications` / `markNotificationRead` / `markAllNotificationsRead` | `/notifications...` |
| 上传 | `uploadImage` | `POST /upload`（multipart） |

主要模型见 `model/Models.kt`：`Banner`、`Category`、`Product`、`User`、`CartItem`、`Order`、`Address`、`AfterSale`、`Notification`、`Review` 等。

---

## 核心类说明

### AuthStore

- SharedPreferences 名：`auth_prefs`
- 保存 `token`、`user`（JSON）
- 提供 `isLoggedIn()`、`logout()`

### CartManager

- 单例，内存维护 `List<CartItem>`
- 支持加购、改数量、删除、清空
- **不持久化、不同步服务端**；进程结束后清空

### ApiClient

- 同步 `OkHttpClient.execute()`，返回 `Result<T>`
- 自动附加 Bearer Token
- 解析统一 `{ success, data, error }` 包装

---

## 构建与依赖

| 配置 | 值 |
|---|---|
| AGP | 8.13.0 |
| Kotlin Compose 插件 | 2.1.0 |
| Compose BOM | 2024.10.00 |
| Java 兼容 | 11 |
| Release | R8 混淆 + 资源压缩，签名配置在 `app/build.gradle.kts` |

版本统一管理于 `gradle/libs.versions.toml`。

---

## 环境要求与运行

### 环境

- Android Studio（建议较新稳定版）
- JDK 17 / 20 / 21（项目 Gradle Daemon 可配置 toolchain）
- Android SDK（compileSdk 36）
- **后端服务**监听 `localhost:3456`，提供 `/api/*`（与 `ApiClient` 路径一致）

### 构建命令

```bash
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK（需 keystore）
./gradlew installDebug       # 安装到已连接设备/模拟器
```

Windows 可用 `gradlew.bat`。产物一般在 `app/build/outputs/apk/`。

### 运行步骤

1. 启动后端（端口 **3456**）
2. 确认 `ApiClient.BASE_URL` 与设备环境匹配（模拟器用 `10.0.2.2`，真机用局域网 IP）
3. Android Studio 打开工程 → Sync → Run `app`
4. 或命令行执行 `assembleDebug` / `installDebug`

### 镜像说明

- Gradle 分发包可走腾讯云 CDN（见 `gradle-wrapper.properties`）
- Maven 仓库配置见 `settings.gradle.kts`（google + mavenCentral）

---

## 注意事项

1. **依赖后端**：无内置 Mock，离线无法完整演示业务。
2. **支付为模拟**：`PaymentScreen` 调用 `payOrder`，非真实支付通道。
3. **购物车仅本地**：与账号、多端不同步。
4. **网络配置**：当前 `AndroidManifest.xml` 若未声明 `INTERNET`，或未允许 HTTP cleartext（Base URL 为 `http://`），联网会失败；Android 9+ 明文 HTTP 需开启 `usesCleartextTraffic` 或配置 Network Security Config。
5. **Release 签名**：keystore 密码等敏感信息建议迁到 `local.properties` / 环境变量，勿提交公开仓库。
6. **架构定位**：适合教学 / 原型的 Compose 商城客户端，非严格 Clean Architecture。

---

## 主题

品牌主色见 `ui/theme/Colors.kt`（如 `BrandPrimary` 红色系），主题入口为 `MallTheme`（`ui/theme/Theme.kt`）。
