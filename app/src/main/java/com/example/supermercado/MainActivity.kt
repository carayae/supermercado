package com.example.supermercado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.supermercado.db.AppDataBase
import com.example.supermercado.db.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //resources.getString(R.string.no_productos)
        lifecycleScope.launch( Dispatchers.IO ) {

            val productoDao = AppDataBase.getInstance(this@MainActivity).productoDao()
            val cantidadRegistros = productoDao.contar()
            if (cantidadRegistros < 1) {
                productoDao.insertarProducto(Producto(0, "limon", false))
                productoDao.insertarProducto(Producto(0, "tomate", true))
                productoDao.insertarProducto(Producto(0, "lechuga", false))
            }
        }

        setContent {
            ListarPorductosUI()
        }
    }
}

@Composable
fun ListarPorductosUI(){
    val contexto = LocalContext.current
    val (productos, setProductos) = remember { mutableStateOf(emptyList<Producto>()) }

    LaunchedEffect( productos ){

        withContext(Dispatchers.IO){
            val dao = AppDataBase.getInstance(contexto).productoDao()
            setProductos(dao.getAllProductos())
        }

    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        items(productos){producto ->
            productoItemUI(producto){

                setProductos(emptyList<Producto>())
            }

        }
    }
}

@Composable
fun productoItemUI(producto: Producto, onSave:()-> Unit = {}){

    val contexto = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp)
    ){
        if (!producto.comprado){
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = "Producto No comprado",
                modifier = Modifier.clickable{
                    alcanceCorrutina.launch(Dispatchers.IO) {
                        val dao = AppDataBase.getInstance(contexto).productoDao()
                        producto.comprado = true
                        dao.actualizarProducto(producto)
                        onSave()
                    }
                }
            )
        }else{
            Icon(
                Icons.Filled.Check,
                contentDescription = "Producto Comprado",
                modifier = Modifier.clickable{
                    alcanceCorrutina.launch(Dispatchers.IO) {
                        val dao = AppDataBase.getInstance(contexto).productoDao()
                        producto.comprado = false
                        dao.actualizarProducto(producto)
                        onSave()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = producto.nombre,
            modifier = Modifier.weight(2f)
        )

        Icon(
            Icons.Filled.Delete,
            contentDescription = "Producto Eliminado",
            modifier = Modifier.clickable{
                alcanceCorrutina.launch(Dispatchers.IO) {
                    val dao = AppDataBase.getInstance(contexto).productoDao()
                    dao.eliminarProducto(producto)
                    onSave()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductoItemPreview(){
    val producto = Producto(1, "brocoli", true)
    productoItemUI(producto)
}

@Preview(showBackground = true)
@Composable
fun ProductoItemPreview2(){
    val producto = Producto(2   , "coliflor", false)
    productoItemUI(producto)
}
