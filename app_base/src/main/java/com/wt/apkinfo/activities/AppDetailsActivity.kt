package com.wt.apkinfo.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.wt.apkinfo.app.AppBuildType
import com.wt.apkinfo.base.BuildConfig
import com.wt.apkinfo.base.R
import com.wt.apkinfo.data.ApplicationDetailsInfo
import com.wt.apkinfo.data.ApplicationEntryInfo
import com.wt.apkinfo.data.models.ApplicationDetailsViewModel
import com.wt.apkinfo.data.models.DetailsUiState
import com.wt.apkinfo.era.ERA

class AppDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: ApplicationDetailsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val pkg = intent.getStringExtra("pkg") ?: return finish()
        viewModel = ViewModelProvider(this).get(ApplicationDetailsViewModel::class.java)
        
        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.observeAsState(DetailsUiState.Loading)

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_details)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (val state = uiState) {
                            is DetailsUiState.Loading -> {
                                CircularProgressIndicator(Modifier.align(Alignment.Center))
                            }
                            is DetailsUiState.Success -> {
                                AppDetailsContent(state.data)
                            }
                            is DetailsUiState.Error -> {
                                ErrorView(state.message) { finish() }
                            }
                        }
                    }
                }
            }
        }

        viewModel.fetchInfo(pkg)
    }

    @Composable
    fun AppDetailsContent(data: ApplicationDetailsInfo) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppDetailsHeader(data)
            
            // Sekcja Akcji (do implementacji)
            ActionsSection(data)
            
            // Tutaj dodasz kolejne sekcje (SDK, Czas instalacji itd.)
        }
    }

    @Composable
    fun AppDetailsHeader(data: ApplicationDetailsInfo) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder dla ikony (zastąp Coilem jak będziesz gotowy)
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("LOGO", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = data.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = data.pkg ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (data.isDebuggable || data.isSystemApp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { 
                                Text(
                                    if (data.isDebuggable) stringResource(R.string.app_type_debug) 
                                    else stringResource(R.string.app_type_system)
                                ) 
                            }
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(label = stringResource(R.string.version_code), value = "${data.versionName} (${data.versionCode})")
                InfoRow(label = stringResource(R.string.installer), value = data.installerPackage ?: "None")
            }
        }
    }

    @Composable
    fun ActionsSection(data: ApplicationDetailsInfo) {
        // Tu na razie puste, ale możesz tu dodać Row z przyciskami
        // wywołującymi np. handleUninstallClick(data.pkg)
    }

    @Composable
    fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.bodySmall)
        }
    }

    @Composable
    fun ErrorView(message: String, onClose: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onClose, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }

    // --- Metody pomocnicze zachowane do użycia w Compose ---

    private fun handleUninstallClick(pkg: String?) {
        pkg ?: return
        try {
            startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.app_run_error, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun handleAppInfoClick(pkg: String?) {
        pkg ?: return
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg")))
        } catch (e: Exception) {
            ERA.logException(e)
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
            } catch (w2: Exception) {
                Toast.makeText(this, getString(R.string.app_run_error, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleMarketClick(pkg: String) {
        val uri = if (AppBuildType.HUAWEI == BuildConfig.BUILD_FOR_MARKET) {
            Uri.parse("appmarket://details?id=$pkg")
        } else {
            Uri.parse("market://details?id=$pkg")
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                if (AppBuildType.GOOGLE == BuildConfig.BUILD_FOR_MARKET) setPackage("com.android.vending")
            })
        } catch (e: Exception) {
            val webUri = if (AppBuildType.HUAWEI == BuildConfig.BUILD_FOR_MARKET) {
                Uri.parse("https://appgallery.huawei.com/app/C101754683")
            } else {
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (e2: Exception) {
                ERA.logException(Exception("No Market app and WebBrowser"))
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ApkInfo", text))
    }

    companion object {
        @JvmStatic
        fun show(ctx: Context, appInfo: ApplicationEntryInfo?) {
            val intent = Intent(ctx, AppDetailsActivity::class.java).apply {
                putExtra("pkg", appInfo?.pkg)
            }
            ctx.startActivity(intent)
        }
    }
}
