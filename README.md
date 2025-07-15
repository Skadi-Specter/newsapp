# NewsApp

## 项目简介

NewsApp（智阅新闻） 是一个基于 Android 的新闻聚合与AI助手应用，支持新闻浏览、AI聊天、AI图像生成、语音朗读等功能。

## 主要功能

- 新闻聚合与分类浏览
- 新闻详情、评论、收藏、历史记录
- AI 聊天助手
- AI 图像生成
- 新闻内容语音朗读（TTS）
- 用户登录与会话管理

## 依赖环境

- Android Studio Giraffe 或更高版本
- JDK 11+
- Gradle 8+
- 主要依赖库：
  - Retrofit2
  - Gson
  - OkHttp
  - Room
  - Glide/Picasso
  - Material Components
  - 及其他常用 Android 支持库

## 构建与运行

1. **克隆项目**
   ```bash
   git clone https://github.com/Skadi-Specter/newsapp.git
   ```

2. **配置三类API密钥**
   - 按下文说明，分别在对应的Java文件中填写你的密钥。

3. **导入 Android Studio**
   - 使用 Android Studio 打开项目根目录，自动同步依赖。

4. **编译与运行**
   - 连接 Android 设备或启动模拟器，点击“运行”按钮即可。

## 重要说明：API密钥配置

本项目涉及三类API密钥，**请务必在首次运行前完成如下配置**：

### 1. 新闻API密钥（News API Key）
- **聚合数据**：https://www.juhe.cn
- **配置位置**：
  `app/src/main/java/com/example/newsapp/network/NewsApi.java`
- **配置方法**：
  找到如下代码，将`NEWS_API_KEY`的值替换为你自己的新闻API密钥（）：
  ```java
  // 新闻API密钥
  String NEWS_API_KEY = "你的AppKey";
  ```
- **用途**：用于获取新闻列表和新闻详情。

---

### 2. AI密钥（AI API Key）
- **硅基流动**：https://www.siliconflow.cn
- **配置位置**：
  `app/src/main/java/com/example/newsapp/network/ai/AiApiService.java`
- **配置方法**：
  找到如下代码，将`API_KEY`的值替换为你自己的AI密钥：
  ```java
  String API_KEY = "你的AI密钥";
  ```
- **用途**：用于AI聊天、AI图像生成（需要将“生成图片。”放在语句开头）功能。

---

### 3. 语音合成密钥（TTS API Key）
- **硅基流动**：https://www.siliconflow.cn
- **配置位置**：
  `app/src/main/java/com/example/newsapp/network/tts/TtsApiService.java`
- **配置方法**：
  找到如下代码，将`API_KEY`的值替换为你自己的TTS密钥：
  ```java
  String API_KEY = "你的TTS密钥";
  ```
- **用途**：用于新闻内容的语音朗读功能。
"# newsapp" 
