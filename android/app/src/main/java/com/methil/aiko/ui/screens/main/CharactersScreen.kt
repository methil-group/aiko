package com.methil.aiko.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.methil.aiko.bridge.AikoConfig
import com.methil.aiko.domain.Character
import com.methil.aiko.ui.components.*
import com.methil.aiko.ui.theme.DarkPurple
import com.methil.aiko.ui.theme.LightViolet
import com.methil.aiko.ui.theme.LightestPink
import com.methil.aiko.ui.viewmodels.MainViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.methil.aiko.R
import com.methil.aiko.ui.theme.DarkPurple
import com.methil.aiko.ui.theme.LightViolet
import com.methil.aiko.ui.theme.LightestPink

@Composable
fun CharactersScreen(
    sessionToken: String,
    mainViewModel: MainViewModel,
    onCharacterSelect: (Character) -> Unit = {}
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val characters = uiState.characters
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    LaunchedEffect(sessionToken) {
        mainViewModel.loadCharacters()
    }

    AikoBackground {
        if (isLoading) {
            CircularProgressIndicator(color = DarkPurple)
        } else if (errorMessage != null) {
            Text(
                text = stringResource(R.string.error_loading_characters, errorMessage!!), 
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.Monospace
            )
        } else if (characters.isEmpty()) {
            Text(
                text = stringResource(R.string.no_characters_found),
                fontFamily = FontFamily.Monospace,
                color = DarkPurple
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AikoHeader(text = stringResource(R.string.characters_title))
                }
                
                items(characters) { character ->
                    AikoCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCharacterSelect(character) },
                        borderColor = DarkPurple
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (character.image_url != null) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.Black)
                                        .border(2.dp, DarkPurple, RectangleShape)
                                ) {
                                    AsyncImage(
                                        model = "${AikoConfig.BASE_URL}${character.image_url}",
                                        contentDescription = character.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = character.name.uppercase(),
                                        style = androidx.compose.ui.text.TextStyle(
                                            color = DarkPurple,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                    if (character.is_public) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = DarkPurple,
                                            shape = RectangleShape
                                        ) {
                                            Text(
                                                text = stringResource(R.string.characters_public_tag),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = androidx.compose.ui.text.TextStyle(
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = character.model_name,
                                    style = androidx.compose.ui.text.TextStyle(
                                        color = DarkPurple.copy(alpha = 0.6f),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                            
                            Icon(
                                painter = painterResource(id = R.drawable.send_ico), // Using send icon as a chevron for now
                                contentDescription = null,
                                tint = DarkPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
