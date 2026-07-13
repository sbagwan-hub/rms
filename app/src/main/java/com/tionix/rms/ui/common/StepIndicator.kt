package com.tionix.rms.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StepIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            StepItem(
                stepNumber = index + 1,
                stepLabel = step,
                isCompleted = index < currentStep,
                isCurrent = index == currentStep,
                isLast = index == steps.size - 1
            )
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    stepLabel: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepCircle(
            stepNumber = stepNumber,
            isCompleted = isCompleted,
            isCurrent = isCurrent
        )
        
        if (!isLast) {
            StepLine(isCompleted = isCompleted, isCurrent = isCurrent)
        }
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val color = when {
        isCompleted -> Color(0xFF4CAF50)
        isCurrent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val backgroundColor = when {
        isCompleted -> Color(0xFF4CAF50)
        isCurrent -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    Canvas(
        modifier = Modifier.size(32.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val radius = size.width / 2 - strokeWidth / 2
        
        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )
        
        if (isCompleted || isCurrent) {
            drawCircle(color = backgroundColor, radius = radius - strokeWidth / 2)
        }
    }
    
    if (!isCompleted) {
        Text(
            text = stepNumber.toString(),
            color = if (isCurrent) Color.White else color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun StepLine(
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val color = when {
        isCompleted -> Color(0xFF4CAF50)
        isCurrent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Canvas(
        modifier = Modifier
            .size(width = 40.dp, height = 2.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx()
        )
    }
}
