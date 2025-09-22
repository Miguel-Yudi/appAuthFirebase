package com.example.emaillogin

import android.content.ContentValues.TAG
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emaillogin.ui.theme.EmailLoginTheme
import com.example.emaillogin.ui.theme.fundo
import com.example.emaillogin.ui.theme.texto
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.emaillogin.R
import com.example.emaillogin.ui.theme.EmailLoginTheme
import com.example.emaillogin.ui.theme.botao
import com.example.emaillogin.ui.theme.card
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.auth.AuthState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmailLoginTheme {
                val navController = rememberNavController() //Variável reponsável por cuidar da navegação no app
                Scaffold( modifier = Modifier.fillMaxSize() ) { paddingValues ->
                    NavHost( //função responsavel pela navegação
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("login") { //Tela de Login
                            TelaLogin(
                                onLogin = { userName ->
                                    navController.navigate("principal")//Vai para tela principal, passando um o nome do usuário como parametro
                                },
                                onRegisterClick = {
                                    navController.navigate("cadastro") //Vai para tela de cadastro
                                }
                            )
                        }
                        composable("cadastro") { //Tela de cadastro
                            TelaCadastro (
                                onRegisterComplete = {
                                    navController.navigate("login") //Vai para tela de login
                                },
                                onLoginClick = {
                                    navController.navigate("login") //Vai para tela de login
                                }
                            )
                        }
                        composable("principal", //Tela principal)
                        ){ backStackEntry ->

                            TelaPrincipal (
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) // limpa tudo da pilha
                                    }
                                },
                                onClick = {
                                    navController.navigate("cadastrodb") //Vai para tela de login
                                }
                            )
                        }

                        composable("cadastrodb", //Tela principal)
                        ){ backStackEntry ->
                            TelaCadastroDb (
                                onRegisterComplete = {
                                    navController.navigate("principal") //Vai para tela de login
                                },
                                onPrincipalClick = {
                                    navController.navigate("princiapl") //Vai para tela de login
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun TelaLogin(
        onLogin: (String) -> Unit,
        onRegisterClick: () -> Unit
    ){

        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var mostrarSenha by remember { mutableStateOf(false) } //varivel que será utlizada para ocultar ou mostrar a senha no campo
        var errorMessage by remember { mutableStateOf<String?>(null) } //Varivel que armazena a mensagem de possíveis erros
        var isLoading by remember {mutableStateOf((false))}
        val auth = remember { FirebaseAuth.getInstance() }

        Column(modifier = Modifier //Corpo da tela
            .background(fundo)
            .fillMaxSize()
            .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.rose1), //Imagem que fica em cima do formulário
                contentDescription = "Rosa azul",
                modifier = Modifier
                    .size(125.dp)
                    .padding(bottom = 16.dp)
            )

            Text(text = "Login", color = texto, fontFamily = FontFamily.Monospace, fontSize = 28.sp) //Título

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }


            Spacer(modifier = Modifier.height(40.dp))

            TextField( //Campo de email
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email", color = Color.Black) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField( //campo de senha
                value = senha,
                onValueChange = { senha = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Senha", color = Color.Black) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black),
                shape = RoundedCornerShape(size = 20.dp),
                visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                        Icon(
                            painter = painterResource(
                                id = if (mostrarSenha) R.drawable.visivel else R.drawable.invisivel
                            ),
                            contentDescription = "Toggle password visibility",
                            tint = Color.Gray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            //Botão de logar
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    auth.signInWithEmailAndPassword(email,senha)
                        .addOnCompleteListener { task ->
                            isLoading =false
                            if(task.isSuccessful){
                                onLogin(task.result?.user?.uid ?: email)
                            }else {
                                errorMessage = task.exception?.localizedMessage
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = botao
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if(isLoading){
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }else{
                    Text("Login", fontSize = 16.sp)
                }
            }
            errorMessage?.let{
                Spacer(modifier =  Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            //Botão que direciona para tela de cadastro
            Button(onClick = {
                onRegisterClick() //Realiza a função onRegisterClick
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = botao
                ),
                border = BorderStroke(1.dp, botao),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastre-se", fontSize = 16.sp)
            }
        }
    }

    @Composable
    fun TelaCadastro(
        onRegisterComplete: () -> Unit,
        onLoginClick: () -> Unit
    ){
        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var nome by remember { mutableStateOf("") }
        var apelido by remember { mutableStateOf("") }
        var telefone by remember { mutableStateOf("") }
        var mostrarSenha by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") } //Varivel que armazena a mensagem de possíveis erros
        val auth : FirebaseAuth = FirebaseAuth.getInstance()
        val _authState = MutableLiveData<AuthState>()


        Column(modifier = Modifier //Corpo da tela
            .background(fundo)
            .fillMaxSize()
            .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Card( //Card que contem o formulário
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(card)
            ) {
                Column(//Formulario
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(card)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Image(painter = painterResource(id = R.drawable.rose1), //Imagem que fica em cima do formulário
                        contentDescription = "Rosa azul",
                        modifier = Modifier
                            .size(125.dp)
                            .padding(bottom = 16.dp)
                    )

                    Text(text = "Cadastro", color = texto, fontFamily = FontFamily.Monospace, fontSize = 28.sp) //Título

                    Spacer(modifier = Modifier.height(40.dp))

                    TextField( //Campo de email
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(text = "Email", color = Color.Black) },
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextField( //Campo de senha
                        value = senha,
                        onValueChange = { senha = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Senha", color = Color.Black) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black),
                        shape = RoundedCornerShape(size = 20.dp),
                        visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (mostrarSenha) R.drawable.visivel else R.drawable.invisivel
                                    ),
                                    contentDescription = "Toggle password visibility",
                                    tint = Color.Gray
                                )
                            }
                        }
                    )


                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = { //Botão de cadastrar
                        if(email.isEmpty() || senha.isEmpty()){
                            _authState.value = AuthState.Error("Email or password can't be empty")
                            return@Button
                        }
                        _authState.value = AuthState.Loading
                        auth.createUserWithEmailAndPassword(email,senha)
                            .addOnCompleteListener{task->
                                if (task.isSuccessful){
                                    _authState.value = AuthState.Authenticated
                                    onRegisterComplete()
                                }else{
                                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                                }
                            }
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = botao
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cadastrar", fontSize = 16.sp)
                    }

                    Button(onClick = {onLoginClick()}, //Botão que direciona para tela de login
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fazer login", fontSize = 16.sp)
                    }

                }

            }
        }
    }

    @Composable
    fun TelaPrincipal(
        userName: String = "Usuário",
        onLogout: () -> Unit,
        onClick: () -> Unit
    ) {
        var menuAberto by remember { mutableStateOf(false) }
        var mostrarRegistros by remember { mutableStateOf(false) }
        val db = Firebase.firestore
        val banco = remember { mutableStateListOf<Map<String, Any>>() }
        val scrollState = rememberScrollState() // Adicionando estado de scroll

        Column( //Corpo da página
            modifier = Modifier
                .fillMaxSize()
                .background(fundo)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = { menuAberto = true }) {  // Botão  de menu (três pontos)
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = botao
                    )
                }

                DropdownMenu( //Menu que é aberto ao clicar nos três pontos
                    expanded = menuAberto,
                    onDismissRequest = { menuAberto = false }
                ) {
                    DropdownMenuItem( //Opção para mostrar os registros
                        text = { Text("Listar Registros") },
                        onClick = {
                            menuAberto = false
                            db.collection("banco") //Pega as informações do firebase
                                .get() //Comando get
                                .addOnSuccessListener { result -> //Se tudo der certo
                                    banco.clear() //limpa a variável banco
                                    for (document in result) { //para cada registro
                                        banco.add(document.data) //Adiciona instancia na variável
                                    }
                                    mostrarRegistros = true
                                }
                                .addOnFailureListener { exception -> //Se algo der errado no processo
                                    Log.w(TAG, "Error getting documents.", exception)
                                }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cadastrar") },
                        onClick = {
                            menuAberto = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sair") },
                        onClick = {
                            menuAberto = false
                            onLogout()
                        }
                    )
                }

            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rose1),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp)
                )
            }

            Column( //Coluna scrolavel
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // Adicionando scroll aqui
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Bem-vindo, registre contas!",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                if (mostrarRegistros) {


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 40.dp) // Adicionando padding bottom para espaço
                    ) {
                        banco.forEachIndexed { index, registro ->
                            Column( //Corpo do card de registro
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                                    .padding(0.dp,0.dp,0.dp,12.dp)
                            ) {
                                Column( //Coluna com imagem na parte de cima do card
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(card, shape = RoundedCornerShape(20.dp,20.dp,0.dp,0.dp)),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.rose1),
                                        contentDescription = "Logo",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                                Column { //Coluna que contem as informações
                                    Text("Registro ${index + 1}", color = card, fontSize = 18.sp)
                                    Text("Nome: ${registro["nome"]}", color = Color.Black)
                                    Text("Apelido: ${registro["apelido"]}", color = Color.Black)
                                    Text("Email: ${registro["email"]}", color = Color.Black)
                                    Text("Senha: ${registro["senha"]}", color = Color.Black)
                                    Text("Telefone: ${registro["telefone"]}", color = Color.Black)

                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Use o menu no canto superior direito para listar os registros",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }

        @Composable
        fun TelaCadastroDb(
            onRegisterComplete: () -> Unit,
            onPrincipalClick: () -> Unit
        ) {
            var email by remember { mutableStateOf("") }
            var senha by remember { mutableStateOf("") }
            var nome by remember { mutableStateOf("") }
            var apelido by remember { mutableStateOf("") }
            var telefone by remember { mutableStateOf("") }
            var mostrarSenha by remember { mutableStateOf(false) }
            val db = Firebase.firestore
            var errorMessage by remember { mutableStateOf("") } //Varivel que armazena a mensagem de possíveis erros


            Column(
                modifier = Modifier //Corpo da tela
                    .background(fundo)
                    .fillMaxSize()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card( //Card que contem o formulário
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(card)
                ) {
                    Column(
                    //Formulario
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(card)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rose1), //Imagem que fica em cima do formulário
                            contentDescription = "Rosa azul",
                            modifier = Modifier
                                .size(125.dp)
                                .padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Cadastro",
                            color = texto,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 28.sp
                        ) //Título

                        Spacer(modifier = Modifier.height(40.dp))

                        TextField( //Campo de nome
                            value = nome,
                            onValueChange = { nome = it },
                            label = { Text(text = "Nome", color = Color.Black) },
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TextField( //Campo de apelido
                            value = apelido,
                            onValueChange = { apelido = it },
                            label = { Text(text = "Apelido", color = Color.Black) },
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TextField( //Campo de email
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(text = "Email", color = Color.Black) },
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TextField( //Campo de senha
                            value = senha,
                            onValueChange = { senha = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Senha", color = Color.Black) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(size = 20.dp),
                            visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (mostrarSenha) R.drawable.visivel else R.drawable.invisivel
                                        ),
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TextField( //Campo de telefone
                            value = telefone,
                            onValueChange = { telefone = it },
                            label = { Text(text = "Telefone", color = Color.Black) },
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { //Botão de cadastrar
                                if (nome.isBlank() || apelido.isBlank() || email.isBlank() || senha.isBlank()) { //Mensagem de erro caso algum dos campos esteja em branco
                                    errorMessage = "Preencha todos os campos obrigatórios"
                                    return@Button
                                }

                                val usuario =
                                    hashMapOf( //Criar uma variável que contem um hashMap das informações passadas
                                        "nome" to nome,
                                        "apelido" to apelido,
                                        "email" to email,
                                        "senha" to senha,
                                        "telefone" to telefone
                                    )

                                db.collection("banco") // Pega as informações armazenadas no firebase
                                    .add(usuario) //Tenta adicionar o usuário
                                    .addOnSuccessListener { //Se conseguir
                                        Log.d("Firestore", "Documento adicionado com ID: ${it.id}")
                                        onRegisterComplete() //Realiza função onRegisterComplete
                                    }
                                    .addOnFailureListener { e -> //Caso ocorra um erro no processo
                                        errorMessage = "Erro ao cadastrar: ${e.message}"
                                        Log.w("Firestore", "Erro ao adicionar documento", e)
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = botao
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cadastrar", fontSize = 16.sp)
                        }

                        Button(
                            onClick = { onPrincipalClick() }, //Botão que direciona para tela de login
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tela principal", fontSize = 16.sp)
                        }

                    }

                }
            }

        }




    @Preview
    @Composable
    fun telaLoginPreview(){ //Preview da Tela de Login
        TelaLogin(onLogin = {}, onRegisterClick = {})
    }

    @Preview
    @Composable
    fun telaCadastroPreview(){ //Preview da Tela de Cadastro
        TelaCadastro(onRegisterComplete = {}, onLoginClick = {})
    }

    @Preview
    @Composable
    fun telaPrincipalPreview(){ //Preview da tela principal

    }

    sealed class AuthState{
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message : String) : AuthState()
    }
}



