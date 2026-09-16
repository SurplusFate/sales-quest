package com.salesquest.sales_quest.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.salesquest.sales_quest.ui.AppTab

/** 内容画布玻璃模糊半径 */
private val GlassContentBlur = 26.dp

/** 底部导航玻璃模糊半径 */
private val GlassNavBlur = 30.dp

/**
 * 依据显示模式判断深色主题, 统一读取 SalesQuestTheme 注入的 LocalIsDark。
 * 不再自行用色板亮度推断, 避免浅色模式下玻璃误渲染成深色。
 */
@Composable
private fun currentThemeIsDark(): Boolean = LocalIsDark.current

/**
 * 根画布: 液态玻璃底部的彩色渐变光斑基底。
 * 使用 CanvasBackdrop(纯绘制), 与内容捕获层分离, 避免自引用拖影。
 */
@Composable
private fun rememberRootBackdrop(): Backdrop {
    val dark = currentThemeIsDark()
    return key(dark) {
        rememberCanvasBackdrop {
            val w = size.width
            val h = size.height
            val baseColors = if (dark) {
                listOf(Color(0xFF202B4A), Color(0xFF30407A), Color(0xFF282354))
            } else {
                listOf(Color(0xFFEAF1FB), Color(0xFFDCE6F5), Color(0xFFE8E3F7))
            }
            drawRect(Brush.verticalGradient(baseColors))
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x666C5CE7), Color.Transparent)
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.15f, h * 0.18f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(if (dark) Color(0x55FFB300) else Color(0x55FF9800), Color.Transparent)
                ),
                radius = w * 0.50f,
                center = Offset(w * 0.85f, h * 0.30f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(if (dark) Color(0x5564B5F6) else Color(0x5542A5F5), Color.Transparent)
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.55f, h * 0.85f)
            )
        }
    }
}

/**
 * 液态玻璃根容器(三层结构避免自引用拖影):
 *  - 层1: 渐变基底画布(CanvasBackdrop)
 *  - 层2: 内容模糊背板(采样 contentBackdrop 模糊后作为整页背景)
 *  - 层3: 内容捕获层(仅捕获页面内容, 不含底部栏) + 底部栏(可引用 contentBackdrop 采样)
 *
 * @param bottomBar 底部栏槽位, 可拿到 contentBackdrop 实现玻璃导航栏
 * @param content   页面内容, 同样可拿到 contentBackdrop
 */
@Composable
fun GlassRoot(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (contentBackdrop: LayerBackdrop) -> Unit = {},
    content: @Composable (contentBackdrop: LayerBackdrop) -> Unit
) {
    val rootBackdrop = rememberRootBackdrop()
    // 深浅切换时强制重建内容捕获层:
    // LayerBackdrop 录制的是绘制快照, 主题切换后旧缓存不会自动失效,
    // 会导致"背景已切换、内容仍是旧模式快照"的混合画面(切后台回来 Surface 重建才刷新)。
    // 用 key(dark) 强制丢弃旧 GraphicsLayer 与 LayerBackdrop, 立即以新主题重录内容。
    val dark = currentThemeIsDark()
    val contentLayer = key(dark) { rememberGraphicsLayer() }
    // onDraw 必须重绘子树内容(drawContent), 否则 contentLayer 录制为空,
    // 层2模糊背板与玻璃导航栏采样到空白, 磨砂模糊不渲染
    val contentBackdrop = key(dark) { rememberLayerBackdrop(contentLayer) { drawContent() } }

    Box(modifier.fillMaxSize()) {
        // 层0: 主题背景兜底(保证状态栏/边缘区域与页面同色, 不透出系统白底)
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        // 层1: 渐变基底(最底, 不模糊)
        Box(
            Modifier
                .fillMaxSize()
                .drawBackdrop(
                    backdrop = rootBackdrop,
                    shape = { RectangleShape },
                    effects = {}
                )
        )
        // 层2: 内容模糊背板(画在内容下方, 页面坐在磨砂玻璃上)
        Box(
            Modifier
                .fillMaxSize()
                .drawBackdrop(
                    backdrop = contentBackdrop,
                    shape = { RectangleShape },
                    effects = { blur(GlassContentBlur.toPx(), TileMode.Clamp) }
                )
        )
        // 层3: 内容捕获层 —— 铺满全屏, 页面背景延伸到底部,
        // 悬浮 DOCK 才能采样到其正下方真实内容做液态玻璃。
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(contentBackdrop)
        ) {
            content(contentBackdrop)
        }
        // 层4: 底部栏 —— 悬浮叠加在内容之上, 不占布局,
        // 采样 contentBackdrop 模糊后即得磨砂玻璃效果(对齐 GenshinGachaHelper RoundedBottomDock)。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            bottomBar(contentBackdrop)
        }
    }
}

/**
 * 玻璃底部栏容器: 采样 contentBackdrop(页面内容) 模糊作为背景, 形成磨砂玻璃导航栏。
 */
@Composable
fun GlassNavigationBar(
    contentBackdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = contentBackdrop,
                shape = { RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) },
                effects = { blur(GlassNavBlur.toPx(), TileMode.Clamp) }
            )
    ) {
        content()
    }
}

/**
 * 玻璃面板修饰符(卡片/列表项用): 半透明底 + 细白边, 叠加在模糊画布上呈现液态玻璃质感。
 */
@Composable
fun rememberGlassSurfaceModifier(
    cornerRadius: Dp = 16.dp,
    tintAlpha: Float = 0.55f,
    borderAlpha: Float = 0.30f
): Modifier {
    val dark = currentThemeIsDark()
    // 浅色: 低透明度白(通透磨砂) + 淡白边; 深色: 提亮深蓝半透明 + 弱白边
    val tint = if (dark) Color(0x3D1B2544) else Color(0x59FFFFFF)
    val borderColor = Color.White.copy(alpha = if (dark) 0.20f else 0.55f)
    val shape = RoundedCornerShape(cornerRadius)
    return Modifier
        .background(tint.copy(alpha = tintAlpha), shape)
        .border(1.dp, borderColor.copy(alpha = borderAlpha), shape)
}

/**
 * 圆角胶囊底部 DOCK（对齐 GenshinGachaHelper 的 RoundedBottomDock 形态）:
 * 悬浮胶囊、左右留边、不贴底; 采样 contentBackdrop 做磨砂玻璃背景;
 * 选中项为胶囊内一块更亮的玻璃底片 + 顶部高光, 图标文字高对比。
 */
@Composable
fun GlassDock(
    contentBackdrop: LayerBackdrop,
    tabs: List<AppTab>,
    selectedTab: AppTab?,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = currentThemeIsDark()
    val capsuleShape = RoundedCornerShape(28.dp)
    val blockShape = RoundedCornerShape(16.dp)
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // 玻璃主体: 半透明白, 透出下层采样内容(对齐 GenshinGachaHelper GlassTokens fillTop/fillBottom)
    val glassTop = if (dark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.62f)
    val glassBottom = if (dark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.34f)
    val glassFill = Brush.verticalGradient(listOf(glassTop, glassBottom))

    // 选中玻璃片: 以主题色轻微着色, 保持品牌色调
    val blockColor by animateColorAsState(
        targetValue = if (selectedTab != null) primary.copy(alpha = if (dark) 0.28f else 0.16f) else Color.Transparent,
        animationSpec = tween(200),
        label = "dockBlockColor"
    )
    val blockEdge = if (dark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.90f)
    val selectedContent by animateColorAsState(
        targetValue = primary,
        animationSpec = tween(160),
        label = "dockSelectedContent"
    )
    val unselectedContent = onSurfaceVariant.copy(alpha = if (dark) 0.88f else 0.72f)

    val capsuleHeight = 62.dp
    val blockHeight = 46.dp
    val blockSideInset = 6.dp

    val baseModifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp)
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(bottom = 10.dp)
        .height(capsuleHeight)

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = baseModifier
                .drawBackdrop(
                    backdrop = contentBackdrop,
                    shape = { capsuleShape },
                    effects = { blur(GlassNavBlur.toPx(), TileMode.Clamp) }
                )
                .clip(capsuleShape)
                .background(glassFill)
                .border(
                    width = 1.dp,
                    brush = if (dark) Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.08f))
                    ) else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.35f))
                    ),
                    shape = capsuleShape
                )
        ) {
            val itemWidth = maxWidth / tabs.size
            val blockWidth = itemWidth - blockSideInset * 2
            val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
            val blockOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex + blockSideInset,
                animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing),
                label = "dockBlockOffset"
            )

            // 下层: 选中玻璃片(小圆角矩形, 仅覆盖当前 tab 图标+文字)
            Box(
                modifier = Modifier
                    .offset(x = blockOffset, y = (capsuleHeight - blockHeight) / 2)
                    .width(blockWidth)
                    .height(blockHeight)
                    .clip(blockShape)
                    .background(blockColor)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(blockEdge, Color.Transparent)),
                        shape = blockShape
                    )
            )

            // 上层: 页签内容(图标 + 文字)
            Row(modifier = Modifier.fillMaxSize()) {
                tabs.forEachIndexed { index, tab ->
                    val selected = tab == selectedTab
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) selectedContent else unselectedContent,
                        animationSpec = tween(140),
                        label = "dockIconColor$index"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (selected) tab.activeIcon else tab.icon,
                                contentDescription = tab.label,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * DOCK 悬浮于内容之上，各页滚动列表需在底部预留的空白高度：
 * 胶囊 62dp + 下沿空隙 10dp + 12dp 视觉余量，另加系统导航栏高度。
 * 列表滚到底时最后一项不会被胶囊压住（对齐 GenshinGachaHelper RoundedBottomDock）。
 */
@Composable
fun dockContentBottomPadding(): Dp =
    84.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()