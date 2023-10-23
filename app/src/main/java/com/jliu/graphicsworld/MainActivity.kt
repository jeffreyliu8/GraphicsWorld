package com.jliu.graphicsworld

import android.graphics.RuntimeShader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import com.jliu.graphicsworld.ui.theme.GraphicsWorldTheme
import org.intellij.lang.annotations.Language

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphicsWorldTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShaderBrushView()
                }
            }
        }
    }
}

@Language("AGSL")
val SHADER_CODE = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform shader contents;


half4 main(float2 fragCoord) {
    vec2 uv = fragCoord / iResolution.xy;
    vec4 fragColor = vec4(uv.x,0,0,1);
    return fragColor;
}
""".trimIndent()



@Composable
@Preview
fun ShaderBrushView(
    modifier: Modifier = Modifier,
) {
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = it / 1000f
            }
        }
    }
    val shader = RuntimeShader(SHADER_CODE)
    Box(
        modifier = modifier
            .onSizeChanged { size ->
                shader.setFloatUniform(
                    "iResolution",
                    size.width.toFloat(),
                    size.height.toFloat()
                )
            }
            .graphicsLayer {
                shader.setFloatUniform("iTime", time)
            }
            .drawWithCache {
                val shaderBrush = ShaderBrush(shader)
                onDrawBehind {
                    drawRect(shaderBrush)
                }
            }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GraphicsWorldTheme {
        ShaderBrushView()
    }
}