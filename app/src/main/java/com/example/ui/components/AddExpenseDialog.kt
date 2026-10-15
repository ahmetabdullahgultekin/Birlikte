package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.ExpenseCategory
import com.example.model.Member
import com.example.ui.theme.BirlikteIndigo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    members: List<Member>,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        paidByMemberId: String,
        splitMemberIds: List<String>,
        category: ExpenseCategory
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidByMemberId by remember { mutableStateOf(members.find { it.isCurrentUser }?.id ?: members.firstOrNull()?.id ?: "") }
    var category by remember { mutableStateOf(ExpenseCategory.FOOD) }

    // Split members (default: all members)
    val selectedSplitIds = remember {
        mutableStateListOf<String>().apply {
            addAll(members.map { it.id })
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                Loc.s("add_expense", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(Loc.s("expense_title", lang)) },
                    placeholder = { Text("Örn: Masa Hesabı, Benzin...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Loc.s("expense_amount", lang)) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category Selection
                Text(
                    "Kategori",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BirlikteIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                Loc.getExpenseCategoryLabel(cat, lang),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Payer Selection
                Text(
                    Loc.s("payer", lang),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    members.forEach { m ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paidByMemberId = m.id }
                        ) {
                            RadioButton(
                                selected = paidByMemberId == m.id,
                                onClick = { paidByMemberId = m.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                m.name + if (m.isCurrentUser) " (Siz)" else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Split With Selection
                Text(
                    Loc.s("split_with", lang),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    members.forEach { m ->
                        val isChecked = selectedSplitIds.contains(m.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedSplitIds.remove(m.id)
                                    else selectedSplitIds.add(m.id)
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedSplitIds.add(m.id)
                                    else selectedSplitIds.remove(m.id)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                m.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0.0) {
                        onConfirm(
                            title,
                            amt,
                            paidByMemberId,
                            selectedSplitIds.toList(),
                            category
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo)
            ) {
                Text(Loc.s("save", lang), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Loc.s("cancel", lang))
            }
        }
    )
}
