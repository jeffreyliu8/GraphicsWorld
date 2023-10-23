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

//https://www.youtube.com/watch?v=f4s1h2YETNY&list=LL&index=4&t=160s&ab_channel=kishimisu
@Language("AGSL")
val SHADER_CODE = """
uniform float2 iResolution;
uniform float iTime;

//https://iquilezles.org/articles/palettes/
vec3 palette(float t) {
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.263, 0.416, 0.557);

    return a + b * cos(6.28318 * (c * t + d));
}

half4 main(float2 fragCoord) {
    vec2 uv = fragCoord / iResolution.xy;
    uv = uv * vec2(1.0, -1.0) + vec2(0.0, 1.0); // flip upside down for origin at bottom left
    uv = (uv - 0.5) * 2.0; // make center (0,0), and top right (1,1), bottom left (-1,-1)

    // handle rotation scaling issue, always in center
    if (iResolution.x > iResolution.y) {
        uv.x *= iResolution.x / iResolution.y;
    } else if (iResolution.x < iResolution.y) {
        uv.y *= iResolution.y / iResolution.x;
    }

    vec2 uv0 = uv;
    vec3 finalColor = vec3(0.0);

    for (float i = 0.0; i < 4.0; i++) {
        uv = fract(uv * 1.5) - 0.5;

        float d = length(uv) * exp(-length(uv0));

        vec3 col = palette(length(uv0) + i * .4 + iTime * .4);

        d = sin(d * 8. + iTime) / 8.;
        d = abs(d);

        d = pow(0.01 / d, 1.2);

        finalColor += col * d;
    }

    half4 fragColor = vec4(finalColor, 1.0);
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
            .drawWithCache {
                val shaderBrush = ShaderBrush(shader)
                shader.setFloatUniform("iTime", time)
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