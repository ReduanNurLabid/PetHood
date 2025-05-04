package com.example.pethood.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pethood.data.PetCategory

@Composable
fun CategorySelector(
    selectedCategory: PetCategory,
    onCategorySelected: (PetCategory) -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        PetCategoryTab(
            category = PetCategory.DOG,
            isSelected = selectedCategory == PetCategory.DOG,
            color = Color(0xFFFF415B),
            onClick = { onCategorySelected(PetCategory.DOG) }
        )

        PetCategoryTab(
            category = PetCategory.CAT,
            isSelected = selectedCategory == PetCategory.CAT,
            color = Color(0xFF6A1B9A),
            onClick = { onCategorySelected(PetCategory.CAT) }
        )
        
        PetCategoryTab(
            category = PetCategory.OTHER,
            isSelected = selectedCategory == PetCategory.OTHER,
            color = Color(0xFF9C27B0), // Purple color for bird category
            onClick = { onCategorySelected(PetCategory.OTHER) }
        )
    }
}

@Composable
fun PetCategoryTab(
    category: PetCategory,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) color else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (category) {
                PetCategory.DOG -> "Dogs"
                PetCategory.CAT -> "Cats"
                PetCategory.OTHER -> "Birds"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}