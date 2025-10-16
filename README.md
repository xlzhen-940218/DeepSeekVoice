# 🤖 DeepSeek 消息朗读助手 (DeepSeek Reader)

## 🌟 项目简介

DeepSeek 消息朗读助手是一个浏览器扩展/App 内嵌脚本，旨在为 DeepSeek 的网页版或 WebView 界面增强功能，提供**文本转语音（Text-to-Speech, TTS）**能力。

本项目特别关注 DeepSeek 官网和 App 客户端中目前缺乏文本朗读功能的问题，致力于为视力障碍、阅读困难或希望解放双手的用户提供无障碍的语音阅读体验。

## ✨ 核心功能与解决的问题

| 功能点 | 描述 | 解决的问题 |
| :--- | :--- | :--- |
| **一键朗读** | 为 DeepSeek 对话中的每个消息块（`ds-message`）底部添加一个**播放按钮**。 | 用户无需手动复制粘贴文本到其他 TTS 工具。 |
| **无障碍访问** | 通过语音播放消息内容，帮助视力不佳或有阅读障碍的用户轻松获取信息。 | 提升 DeepSeek 平台的可访问性和包容性。 |
| **WebView 兼容** | 针对 Android WebView 环境，设计了优雅的 **JavaScript 桥接回退机制**。 | 解决了原生 WebView 不支持 Web Speech API (TTS) 的行业难题，确保在 App 内嵌场景中功能可用。 |
| **性能优化** | 使用数据属性标记机制 (`data-speech-button-added`)，防止重复添加事件监听器和按钮，保持性能高效。 | 避免资源浪费和潜在的运行时错误。 |

## 🛠️ 技术实现细节

本项目主要通过 JavaScript 实现，并依赖于宿主环境（浏览器或原生 App）提供的 TTS 能力。

### 1. 语音播放逻辑（`playText` 函数）

项目采用双重策略确保最高的兼容性：

| 优先级 | 技术/方法 | 适用环境 | 说明 |
| :--- | :--- | :--- | :--- |
| **1 (首选)** | Web Speech API (`window.speechSynthesis`) | Chrome、Firefox 等主流桌面和移动浏览器 | 标准实现，使用浏览器内置的 TTS 引擎。 |
| **2 (回退)** | JavaScript 桥接 (`window.AndroidTTS.speak`) | Android WebView | 当 Web Speech API 不可用时，通过调用 Android 原生 `TextToSpeech` 库来实现语音播放。 |

### 2. 界面和事件绑定

1.  **动态元素创建：** 脚本遍历所有 `ds-message` 元素。
2.  **创建按钮：** 使用 `document.createElement('button')` 创建一个带有 `🔊 播放` 标签的按钮。
3.  **事件处理：** 按钮点击时，使用 `event.stopPropagation()` 阻止事件冒泡到父级 `div`，然后调用 `playText` 函数。
4.  **文本提取：** 使用父级 `div` 的 `innerText`，并移除按钮自身的文本（`replace(playButton.innerText, '')`），确保朗读内容的纯净。
5.  **防止重复：** 通过设置自定义属性 `data-speech-button-added` 来标记已处理的 `div`。

## ⚙️ 部署说明

### 💻 纯网页浏览器环境（如 Chrome 扩展）

将 JavaScript 代码作为**用户脚本**或**内容脚本（Content Script）**注入到 DeepSeek 网页的 DOM 加载完成后执行。

### 📱 Android WebView 环境

对于希望将 DeepSeek 网页打包为 Android App 的开发者，您需要在原生 Java/Kotlin 代码中执行以下操作：

1.  **实现原生 TTS：** 初始化 `android.speech.tts.TextToSpeech` 实例。
2.  **设置 JavaScript 接口：** 使用 `webView.addJavascriptInterface(new WebViewTTSInterface(), "AndroidTTS")` 将原生 TTS 接口暴露给前端。
3.  **桥接方法：** 确保您的原生接口中包含一个使用 `@JavascriptInterface` 注解的 `speak(String text)` 方法，用于接收前端传来的文本并进行朗读。

---

**致谢：** 感谢 DeepSeek 提供的优秀 AI 服务。本项目旨在作为一项辅助功能增强，期望能帮助到更多有需要的人群。
