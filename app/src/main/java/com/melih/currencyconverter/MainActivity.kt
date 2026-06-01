package com.melih.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.DecimalFormat

// ---------- UI state ----------
// One object that describes everything the screen needs to draw itself.
data class UiState(
    val amount: String = "1",
    val from: String = "USD",
    val to: String = "EUR",
    val result: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ---------- ViewModel ----------
// Holds the state and does the network call off the main thread.
class ConverterViewModel : ViewModel() {

    var state by mutableStateOf(UiState())
        private set

    val currencies = listOf("USD", "EUR", "GBP", "TRY", "JPY", "CHF", "CAD", "AUD")

    fun onAmountChange(new: String) {
        state = state.copy(amount = new, result = null, error = null)
    }

    fun onFromChange(code: String) { state = state.copy(from = code, result = null) }
    fun onToChange(code: String)   { state = state.copy(to = code, result = null) }

    fun swap() {
        state = state.copy(from = state.to, to = state.from, result = null)
    }

    fun convert() {
        val amount = state.amount.toDoubleOrNull()
        if (amount == null) {
            state = state.copy(error = "Please enter a valid number")
            return
        }
        if (state.from == state.to) {
            state = state.copy(result = format(amount), error = null)
            return
        }

        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val rate = fetchRate(state.from, state.to)
                state = state.copy(result = format(amount * rate), isLoading = false)
            } catch (e: Exception) {
                state = state.copy(error = "Couldn't load rate. Check your connection.", isLoading = false)
            }
        }
    }

    // Calls the Frankfurter API. No API key needed.
    // Example: https://api.frankfurter.dev/v1/latest?base=USD&symbols=EUR
    private suspend fun fetchRate(from: String, to: String): Double =
        withContext(Dispatchers.IO) {
            val url = "https://api.frankfurter.dev/v1/latest?base=$from&symbols=$to"
            val response = URL(url).readText()
            val rates = JSONObject(response).getJSONObject("rates")
            rates.getDouble(to)
        }

    private fun format(value: Double): String = DecimalFormat("#,##0.00").format(value)
}

// ---------- Activity ----------
class MainActivity : ComponentActivity() {
    private val viewModel: ConverterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConverterScreen(viewModel)
                }
            }
        }
    }
}

// ---------- Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(viewModel: ConverterViewModel) {
    val state = viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Currency Converter", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = state.amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurrencyDropdown(
                label = "From",
                selected = state.from,
                options = viewModel.currencies,
                onSelected = viewModel::onFromChange,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = viewModel::swap) { Text("⇄") }
            CurrencyDropdown(
                label = "To",
                selected = state.to,
                options = viewModel.currencies,
                onSelected = viewModel::onToChange,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = viewModel::convert,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Converting..." else "Convert")
        }

        state.result?.let {
            Text(
                "${state.amount} ${state.from} = $it ${state.to}",
                style = MaterialTheme.typography.titleLarge
            )
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { code ->
                DropdownMenuItem(
                    text = { Text(code) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}