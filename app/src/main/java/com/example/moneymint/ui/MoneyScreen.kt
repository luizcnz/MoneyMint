package com.example.moneymint.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.moneymint.data.Categoria
import com.example.moneymint.data.Gasto
import com.example.moneymint.ui.theme.NeonGreen
import com.example.moneymint.ui.theme.NeonRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyScreen(viewModel: MoneyViewModel) {
    val listaDeGastos by viewModel.gastosFiltrados.collectAsState(initial = emptyList())
    val todasLasTransacciones by viewModel.todasLasTransacciones.collectAsState(initial = emptyList())
    
    val categorias by viewModel.todasLasCategorias.collectAsState(initial = emptyList())
    val filtroActual by viewModel.filtroActual.collectAsState()
    
    var mostrarDialogo by remember { mutableStateOf(false) }
    var menuFiltroExpandido by remember { mutableStateOf(false) }
    var mostrarDialogoFiltroCat by remember { mutableStateOf(false) }
    
    var gastoAEditar by remember { mutableStateOf<Gasto?>(null) }
    var gastoSeleccionadoParaOpciones by remember { mutableStateOf<Gasto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "MoneyMint", 
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
                    IconButton(onClick = { menuFiltroExpandido = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    DropdownMenu(
                        expanded = menuFiltroExpandido,
                        onDismissRequest = { menuFiltroExpandido = false }
                    ) {
                        FiltroGastos.values().forEach { filtro ->
                            DropdownMenuItem(
                                text = { Text(filtro.name.replace("_", " ").lowercase()) },
                                onClick = {
                                    if (filtro == FiltroGastos.POR_CATEGORIA) {
                                        mostrarDialogoFiltroCat = true
                                    } else {
                                        viewModel.cambiarFiltro(filtro)
                                    }
                                    menuFiltroExpandido = false
                                },
                                leadingIcon = {
                                    if (filtroActual == filtro) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    gastoAEditar = null
                    mostrarDialogo = true 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val ingTotal = todasLasTransacciones.filter { it.esIngreso }.sumOf { it.monto }
            val gasTotal = todasLasTransacciones.filter { !it.esIngreso }.sumOf { it.monto }
            val saldoGlobal = ingTotal - gasTotal

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BoxDrawer.neonBorder(MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SALDO TOTAL", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "$${String.format("%.2f", saldoGlobal)}",
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // TEXTO INDICADOR DE FILTRO
            Text(
                text = "Mostrando: ${filtroActual.name.replace("_", " ").lowercase()}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            val ingFiltro = listaDeGastos.filter { it.esIngreso }.sumOf { it.monto }
            val gasFiltro = listaDeGastos.filter { !it.esIngreso }.sumOf { it.monto }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumenMiniCard(Modifier.weight(1f), "Ingresos", ingFiltro, NeonGreen)
                ResumenMiniCard(Modifier.weight(1f), "Gastos", gasFiltro, NeonRed)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaDeGastos) { transaccion ->
                    GastoItem(
                        gasto = transaccion,
                        onLongClick = { gastoSeleccionadoParaOpciones = transaccion }
                    )
                }
            }
        }

        if (mostrarDialogo || gastoAEditar != null) {
            FormularioGasto(
                gastoExistente = gastoAEditar,
                categorias = categorias,
                onDismiss = { mostrarDialogo = false; gastoAEditar = null },
                onConfirm = { monto, desc, catId, fecha, esIngreso ->
                    if (gastoAEditar == null) viewModel.agregarGasto(monto, desc, catId, fecha, esIngreso)
                    else viewModel.actualizarGasto(gastoAEditar!!.copy(monto=monto, descripcion=desc, categoriaId=catId, fecha=fecha, esIngreso=esIngreso))
                    mostrarDialogo = false; gastoAEditar = null
                }
            )
        }

        if (gastoSeleccionadoParaOpciones != null) {
            AlertDialog(
                onDismissRequest = { gastoSeleccionadoParaOpciones = null },
                title = { Text("Opciones", color = MaterialTheme.colorScheme.primary) },
                text = { Text("¿Deseas editar o eliminar este registro?") },
                confirmButton = {
                    TextButton(onClick = { gastoAEditar = gastoSeleccionadoParaOpciones; gastoSeleccionadoParaOpciones = null }) { 
                        Text("Editar", color = MaterialTheme.colorScheme.secondary) 
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.eliminarGasto(gastoSeleccionadoParaOpciones!!); gastoSeleccionadoParaOpciones = null }) { 
                        Text("Eliminar", color = NeonRed) 
                    }
                }
            )
        }

        if (mostrarDialogoFiltroCat) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoFiltroCat = false },
                title = { Text("Filtrar por Categoría", color = MaterialTheme.colorScheme.primary) },
                text = {
                    Column {
                        categorias.forEach { cat ->
                            ListItem(
                                headlineContent = { Text(cat.nombre) },
                                leadingContent = { Text(cat.icono) },
                                modifier = Modifier.clickable {
                                    viewModel.cambiarFiltro(FiltroGastos.POR_CATEGORIA, cat.id)
                                    mostrarDialogoFiltroCat = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.cambiarFiltro(FiltroGastos.MES); mostrarDialogoFiltroCat = false }) { Text("Limpiar") }
                }
            )
        }
    }
}

@Composable
fun ResumenMiniCard(modifier: Modifier, label: String, monto: Double, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BoxDrawer.neonBorder(color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text("$${String.format("%.2f", monto)}", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FormularioGasto(
    gastoExistente: Gasto? = null,
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Int, Long, Boolean) -> Unit
) {
    var monto by remember { mutableStateOf(gastoExistente?.monto?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(gastoExistente?.descripcion ?: "") }
    var esIngreso by remember { mutableStateOf(gastoExistente?.esIngreso ?: false) }
    var categoriaSeleccionada by remember { 
        mutableStateOf(categorias.find { it.id == gastoExistente?.categoriaId } ?: categorias.firstOrNull()) 
    }
    var fechaSeleccionada by remember { mutableStateOf(gastoExistente?.fecha ?: System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BoxDrawer.neonBorder(MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (gastoExistente == null) "NUEVA TRANSACCIÓN" else "EDITAR", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (esIngreso) "INGRESO" else "GASTO", color = if(esIngreso) NeonGreen else NeonRed, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = esIngreso, 
                        onCheckedChange = { esIngreso = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, uncheckedThumbColor = NeonRed)
                    )
                }

                OutlinedTextField(
                    value = monto,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) monto = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = fechaSeleccionada }
                        android.app.DatePickerDialog(context, { _, y, m, d ->
                            cal.set(y, m, d)
                            android.app.TimePickerDialog(context, { _, h, min ->
                                cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, min)
                                fechaSeleccionada = cal.timeInMillis
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text(formatearFecha(fechaSeleccionada))
                }

                Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                    OutlinedTextField(
                        value = categoriaSeleccionada?.nombre ?: "Categoría",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false, // Lo desactivamos para que el click del Box mande
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icono} ${cat.nombre}") },
                                onClick = { categoriaSeleccionada = cat; expanded = false }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        val m = monto.toDoubleOrNull() ?: 0.0
                        onConfirm(m, descripcion, categoriaSeleccionada?.id ?: 0, fechaSeleccionada, esIngreso)
                    }, enabled = monto.isNotBlank() && descripcion.isNotBlank()) { Text("Guardar") }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GastoItem(gasto: Gasto, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BoxDrawer.neonBorder(if (gasto.esIngreso) NeonGreen.copy(alpha = 0.3f) else NeonRed.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = gasto.descripcion, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = formatearFecha(gasto.fecha), fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = (if (gasto.esIngreso) "+" else "-") + "$${String.format("%.2f", gasto.monto)}",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = if (gasto.esIngreso) NeonGreen else NeonRed
            )
        }
    }
}

object BoxDrawer {
    @Composable
    fun neonBorder(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
}

fun formatearFecha(milis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(milis))
}
