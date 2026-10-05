package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParseTreeNode
import com.example.ui.theme.AcademicBackground
import com.example.ui.theme.AcademicTeal
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.max

private data class LayoutNode(
    val node: ParseTreeNode,
    val x: Float,
    val y: Float,
    val children: List<LayoutNode> = emptyList()
)

@Composable
fun ParseTreeCanvas(
    rootNode: ParseTreeNode,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.testTag("parse_tree_canvas_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AcademicBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                    .testTag("parse_tree_canvas")
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Compute Tree Layout
                val layout = computeTreeLayout(rootNode)
                val bounds = computeTreeBounds(layout)

                // Center tree if initial
                val treeW = bounds.second.x - bounds.first.x
                val treeH = bounds.second.y - bounds.first.y

                val baseOffsetX = (canvasW - treeW) / 2f - bounds.first.x + offsetX
                val baseOffsetY = 60f + offsetY

                // Draw tree recursively
                fun drawLayout(ln: LayoutNode) {
                    val px = ln.x * scale + baseOffsetX
                    val py = ln.y * scale + baseOffsetY

                    // Draw connecting lines to children
                    for (child in ln.children) {
                        val cx = child.x * scale + baseOffsetX
                        val cy = child.y * scale + baseOffsetY

                        val path = Path().apply {
                            moveTo(px, py)
                            lineTo(cx, cy)
                        }

                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(px, py),
                            end = Offset(cx, cy),
                            strokeWidth = 2.5f * scale,
                            cap = StrokeCap.Round
                        )

                        drawLayout(child)
                    }

                    // Node Circle
                    val nodeRadius = (if (ln.node.isTerminal) 18f else 22f) * scale
                    val nodeColor = if (ln.node.isTerminal) {
                        if (ln.node.isEpsilon) Color(0xFFF59E0B) else AcademicTeal
                    } else {
                        PrimaryBlue
                    }

                    // Drop shadow simulation
                    drawCircle(
                        color = Color(0x1A000000),
                        radius = nodeRadius + 2f * scale,
                        center = Offset(px, py + 2f * scale)
                    )

                    // Node background
                    drawCircle(
                        color = nodeColor,
                        radius = nodeRadius,
                        center = Offset(px, py)
                    )

                    // Node border
                    drawCircle(
                        color = Color.White,
                        radius = nodeRadius,
                        center = Offset(px, py),
                        style = Stroke(width = 2.0f * scale)
                    )

                    // Text symbol inside node
                    val symbol = ln.node.symbol
                    val textLayout = textMeasurer.measure(
                        text = symbol,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (if (ln.node.isTerminal) 13.sp else 15.sp) * scale,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    val textX = px - textLayout.size.width / 2f
                    val textY = py - textLayout.size.height / 2f

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(textX, textY)
                    )
                }

                drawLayout(layout)
            }

            // Legend / Controls Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(PrimaryBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Non-Terminal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(AcademicTeal, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Terminal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Zoom / Reset Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { scale = (scale + 0.2f).coerceAtMost(3.0f) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("zoom_in_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                FilledIconButton(
                    onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("zoom_out_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                FilledIconButton(
                    onClick = {
                        scale = 1.0f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reset_zoom_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FilterCenterFocus, contentDescription = "Reset View", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Computes non-overlapping 2D layout for the tree.
 */
private fun computeTreeLayout(
    root: ParseTreeNode,
    levelHeight: Float = 75f,
    minNodeSpacing: Float = 55f
): LayoutNode {
    var nextLeafX = 0f

    fun layoutNode(node: ParseTreeNode, depth: Int): LayoutNode {
        val y = depth * levelHeight
        if (node.children.isEmpty()) {
            val x = nextLeafX
            nextLeafX += minNodeSpacing
            return LayoutNode(node, x, y, emptyList())
        }

        val childLayouts = node.children.map { layoutNode(it, depth + 1) }
        val firstChildX = childLayouts.first().x
        val lastChildX = childLayouts.last().x
        val midX = (firstChildX + lastChildX) / 2f

        return LayoutNode(node, midX, y, childLayouts)
    }

    return layoutNode(root, 0)
}

private fun computeTreeBounds(root: LayoutNode): Pair<Offset, Offset> {
    var minX = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var minY = Float.MAX_VALUE
    var maxY = Float.MIN_VALUE

    fun traverse(ln: LayoutNode) {
        if (ln.x < minX) minX = ln.x
        if (ln.x > maxX) maxX = ln.x
        if (ln.y < minY) minY = ln.y
        if (ln.y > maxY) maxY = ln.y
        for (c in ln.children) traverse(c)
    }

    traverse(root)
    return Pair(Offset(minX, minY), Offset(maxX, maxY))
}
