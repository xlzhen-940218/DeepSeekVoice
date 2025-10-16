package com.xlzhen.deepseek.voice;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    private TextToSpeech tts;
    private String currentText;
    private static final String JS_INTERFACE_NAME = "AndroidTTS";

    private static final String VOICE_JS = "function addVoice(){" +
            "/**\n" +
            " * 为消息div添加一个播放按钮，并绑定语音播放事件。\n" +
            " */\n" +
            "\n" +
            "// 1. 获取消息列表\n" +
            "const messages = document.getElementsByClassName('ds-message');\n" +
            "\n" +
            "// 2. 检查浏览器是否支持语音合成API\n" +
            "const isSpeechSupported = 'speechSynthesis' in window;\n" +
            "const ANDROID_TTS_INTERFACE = 'AndroidTTS'; // 假设的Android桥接接口名\n" +
            "\n" +
            "// 3. 封装通用的语音播放逻辑\n" +
            "function playText(text) {\n" +
            "    if (isSpeechSupported) {\n" +
            "        // 方案 A: Web Speech API\n" +
            "        const utterance = new SpeechSynthesisUtterance(text);\n" +
            "        // utterance.lang = 'zh-CN'; // 建议设置语言\n" +
            "        \n" +
            "        window.speechSynthesis.cancel();\n" +
            "        window.speechSynthesis.speak(utterance);\n" +
            "        \n" +
            "    } else if (window[ANDROID_TTS_INTERFACE] && typeof window[ANDROID_TTS_INTERFACE].speak === 'function') {\n" +
            "        // 方案 B: Android WebView 桥接\n" +
            "        // 请确保您的Android原生代码中已实现 AndroidTTS.speak(text) \n" +
            "        window[ANDROID_TTS_INTERFACE].speak(text);\n" +
            "        \n" +
            "    } else {\n" +
            "        // 方案 C: 不支持任何语音功能\n" +
            "        console.error('无法播放语音：设备不支持Web Speech API，且未找到Android桥接接口。');\n" +
            "        // 可选：给用户一个视觉反馈，例如一个简单的提示\n" +
            "        // alert('您的设备不支持语音播放功能。');\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "\n" +
            "// 4. 遍历并处理每个消息元素\n" +
            "for (let i = 0; i < messages.length; i++) {\n" +
            "    const messageDiv = messages[i];\n" +
            "    \n" +
            "    // 【关键步骤】检查是否已经添加过播放按钮（用自定义属性标记）\n" +
            "    if (messageDiv.hasAttribute('data-speech-button-added')) {\n" +
            "        continue;\n" +
            "    }\n" +
            "    \n" +
            "    // 5. 创建播放按钮元素\n" +
            "    const playButton = document.createElement('button');\n" +
            "    playButton.className = 'speech-play-button'; // 添加一个CSS类名，方便后续美化\n" +
            "    \n" +
            "    // 使用简单的文字或一个 Unicode 播放图标\n" +
            "    playButton.innerHTML = '\uD83D\uDD0A 播放'; \n" +
            "    \n" +
            "    // 6. 为按钮添加点击事件\n" +
            "    playButton.addEventListener('click', function(event) {\n" +
            "        // 阻止事件冒泡：如果div本身也有点击事件，此步骤可以阻止它被触发\n" +
            "        event.stopPropagation(); \n" +
            "        \n" +
            "        // 获取父div（即消息本身）的文本内容\n" +
            "        const textToSpeak = messageDiv.innerText.replace(playButton.innerText, '').trim(); \n" +
            "        // 注意：这里需要移除按钮本身的文本，以确保只朗读消息内容\n" +
            "        \n" +
            "        playText(textToSpeak);\n" +
            "    });\n" +
            "    \n" +
            "    // 7. 将按钮添加到div底部\n" +
            "    messageDiv.appendChild(playButton);\n" +
            "    \n" +
            "    // 8. 添加标记属性，表示按钮已添加\n" +
            "    messageDiv.setAttribute('data-speech-button-added', 'true');\n" +
            "}\n" +
            "\n" +
            "// 9. 【可选】添加CSS样式，让按钮看起来更好\n" +
            "const style = document.createElement('style');\n" +
            "style.innerHTML = `\n" +
            ".ds-message {\n" +
            "    position: relative; /* 使内部绝对定位的按钮生效 */\n" +
            "    padding-bottom: 30px; /* 留出按钮的空间 */\n" +
            "}\n" +
            ".speech-play-button {\n" +
            "    /* 将按钮定位在div的右下角 */\n" +
            "    position: absolute;\n" +
            "    bottom: 5px;\n" +
            "    right: 5px;\n" +
            "    \n" +
            "    /* 样式美化 */\n" +
            "    background: #f0f0f0;\n" +
            "    border: 1px solid #ccc;\n" +
            "    border-radius: 4px;\n" +
            "    padding: 2px 8px;\n" +
            "    cursor: pointer;\n" +
            "    font-size: 12px;\n" +
            "}\n" +
            "`;\n" +
            "document.head.appendChild(style);" +
            "}\n" +
            "addVoice();";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 初始化 TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // 可选：设置语言，例如中文
                tts.setLanguage(Locale.CHINA);
            }
        });

        webView = findViewById(R.id.web_view);
        webView.addJavascriptInterface(new WebViewTTSInterface(), JS_INTERFACE_NAME);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("WebView", "Page finished loading: " + url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d("WebView", "Intercepted request: " + request.getUrl());
                if (request.getUrl().toString().contains("https://gator.volces.com/list")) {
                    view.postDelayed(() -> {

                        view.evaluateJavascript(VOICE_JS, null);
                    }, 1000);
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        webView.loadUrl("https://chat.deepseek.com/");
    }

    public class WebViewTTSInterface {

        @JavascriptInterface
        public void speak(String text) {
            // 在主线程中执行 TTS 播放
            runOnUiThread(() -> {
                if (tts != null && (!Objects.equals(currentText, text) || !tts.isSpeaking())) {
                    currentText = text;
                    // 停止任何正在播放的语音
                    tts.stop();
                    // 开始播放
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "uniqueId");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}