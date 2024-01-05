package com.example.supermercado

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supermercado.db.AppDataBase
import com.example.supermercado.db.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppProductosUI()
        }
    }
}

enum class Accion {
    LISTAR, CREAR, EDITAR
}

@Composable
fun AppProductosUI() {
    val contexto = LocalContext.current
    val (productos, setProductos) = remember{ mutableStateOf(
        emptyList<Producto>() ) }
    val (seleccion, setSeleccion) = remember{
        mutableStateOf<Producto?>(null) }
    val (accion, setAccion) = remember{
        mutableStateOf(Accion.LISTAR) }
    LaunchedEffect(productos) {
        withContext(Dispatchers.IO) {
            val db = AppDataBase.getInstance( contexto )
            setProductos( db.productoDao().getAllProductos() )
            Log.v("AppProductosUI", "LaunchedEffect()")
        }
    }
    val onSave = {
        setAccion(Accion.LISTAR)
        setProductos(emptyList())
    }
    when(accion) {
        Accion.CREAR -> ProductoFormUI(null, onSave)
        Accion.EDITAR -> ProductoFormUI(seleccion, onSave)
        else -> ProductosListadoUI(
            productos,
            onAdd = { setAccion( Accion.CREAR ) },
            onEdit = { producto ->
                setSeleccion(producto)
                setAccion( Accion.EDITAR)
            }
        )
    }
}

@Composable
fun ProductosListadoUI(productos:List<Producto>, onAdd:() -> Unit = {},
                       onEdit:(p:Producto) -> Unit = {}) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAdd() },
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "agregar"
                    )
                },
                text = { Text("Agregar") }
            )
        }
    ) { contentPadding ->
        if( productos.isNotEmpty() ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos) { producto ->
                    ProductoItemUI(producto) {
                        onEdit(producto)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay Productos guardados.")
            }
        }
    }
}

@Composable
fun ProductoItemUI(producto: Producto, onClick:() -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Image(
            painter = painterResource(id = R.drawable.producto),
            contentDescription = "Imagen Producto"
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column() {
            Text(producto.nombre, fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp)
        }
    }
}

@Composable
fun ProductoFormUI(p:Producto?, onSave:()->Unit = {}){
    val contexto = LocalContext.current
    val (nombre, setNombre) = remember { mutableStateOf(
        p?.nombre ?: "" ) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost( snackbarHostState) }
    ) {paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.producto),
                contentDescription = "Imagen de Producto")
            TextField(
                value = nombre,
                onValueChange = { setNombre(it) },
                label = {Text("Nombre")}
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = AppDataBase.getInstance( contexto
                    ).productoDao()
                    val producto = Producto(p?.id ?: 0, nombre, comprado = false)
                    if( producto.id > 0) {
                        dao.actualizarProducto(producto)
                    } else {
                        dao.insertarProducto(producto)
                    }
                    snackbarHostState.showSnackbar("Se ha guardado a ${producto.nombre}")
                    onSave()
                }
            }) {
                var textoGuardar = "Crear"
                if(p?.id ?:0 > 0) {
                    textoGuardar = "Guardar"
                }
                Text(textoGuardar)
            }
            if(p?.id ?:0 > 0) {
                Button(onClick = {

                    coroutineScope.launch(Dispatchers.IO) {
                        val dao =
                            AppDataBase.getInstance(contexto).productoDao()
                        snackbarHostState.showSnackbar("Eliminando el producto de ${p?.nombre}")
                        if( p != null) {
                            dao.eliminarProducto(p)
                        }
                        onSave()
                    }
                }) {
                    Text("Eliminar")
                }
            }
        }
    }
}
