package com.example.grainclassifier

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.example.grainclassifier.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grainclassifier.adapter.SupportedGrain
import com.example.grainclassifier.adapter.SupportedGrainsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 0. Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            triggerCamera()
        } else {
            Snackbar.make(binding.root, "Camera permission is required to take photos.", Snackbar.LENGTH_LONG).show()
        }
    }

    // 1. Gallery Result Launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { navigateToPrediction(it) }
    }

    // 2. Camera Result Launcher
    private var cameraImageUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { navigateToPrediction(it) }
        } else {
            Snackbar.make(binding.root, "Photo capture cancelled.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Theme from SharedPreferences Settings
        applySavedTheme()

        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        // Setup Navigation Drawer Toggle
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Highlight Home item initially
        binding.navView.setCheckedItem(R.id.nav_home)

        // Setup Drawer Item Navigation Listener
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            // Select only screen navigation menu items
            if (menuItem.itemId == R.id.nav_home || menuItem.itemId == R.id.nav_realtime || 
                menuItem.itemId == R.id.nav_history || menuItem.itemId == R.id.nav_saved_results || 
                menuItem.itemId == R.id.nav_supported_grains) {
                binding.navView.setCheckedItem(menuItem.itemId)
            }

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already home, do nothing
                }
                R.id.nav_realtime -> {
                    val intent = Intent(this, RealTimeDetectionActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                R.id.nav_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                R.id.nav_saved_results -> {
                    val intent = Intent(this, SavedResultsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                R.id.nav_supported_grains -> {
                    val intent = Intent(this, SupportedGrainsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                R.id.nav_about -> {
                    showAboutProjectDialog()
                }
                R.id.nav_settings -> {
                    showSettingsDialog()
                }
                R.id.nav_help -> {
                    showHelpDialog()
                }
                R.id.nav_exit -> {
                    showExitConfirmationDialog()
                }
            }
            true
        }

        setupClickListeners()
        setupSupportedGrainsRecyclerView()
    }

    private fun applySavedTheme() {
        val sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupClickListeners() {
        // Main action buttons
        binding.btnTakePhoto.setOnClickListener {
            triggerCamera()
        }

        binding.btnUploadImage.setOnClickListener {
            triggerGallery()
        }

        binding.btnRealtimeDetection.setOnClickListener {
            val intent = Intent(this, RealTimeDetectionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.btnViewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // View All Grains click listener
        binding.layoutViewAll.setOnClickListener {
            val intent = Intent(this, SupportedGrainsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupSupportedGrainsRecyclerView() {
        val grainsList = listOf(
            SupportedGrain(
                id = 1,
                name = getString(R.string.grain_rice),
                description = getString(R.string.desc_rice),
                imageResId = R.drawable.grain_rice,
                badgeText = "Ri",
                badgeColorResId = R.color.rice_tag,
                botanicalName = "Oryza sativa",
                details = """
                    • Classification standard: Long Grain / Medium / Short Grain
                    • Target Moisture Threshold: < 14.0%
                    • Average Length: 5.5mm - 7.5mm
                    • Quality Parameters Evaluated: Chalkiness index, head rice yield (HRY), kernel whiteness, broken ratio.
                """.trimIndent()
            ),
            SupportedGrain(
                id = 2,
                name = getString(R.string.grain_wheat),
                description = getString(R.string.desc_wheat),
                imageResId = R.drawable.grain_wheat,
                badgeText = "Wh",
                badgeColorResId = R.color.wheat_tag,
                botanicalName = "Triticum aestivum",
                details = """
                    • Classification standard: Hard Red Winter / Soft White / Durum
                    • Target Moisture Threshold: 12.0% - 13.5%
                    • Average Length: 6.0mm - 8.0mm
                    • Quality Parameters Evaluated: Hectoliter weight, gluten index, foreign organic matter, shriveled seed ratio.
                """.trimIndent()
            ),
            SupportedGrain(
                id = 3,
                name = getString(R.string.grain_maize),
                description = getString(R.string.desc_maize),
                imageResId = R.drawable.grain_maize,
                badgeText = "Mz",
                badgeColorResId = R.color.maize_tag,
                botanicalName = "Zea mays",
                details = """
                    • Classification standard: Yellow Dent / Sweet Corn / Flint Corn
                    • Target Moisture Threshold: < 15.0%
                    • Average Diameter: 8.0mm - 10.0mm
                    • Quality Parameters Evaluated: Aflatoxin PPM ratio, thermal damage ratio, cracked kernel index, seed density.
                """.trimIndent()
            ),
            SupportedGrain(
                id = 4,
                name = getString(R.string.grain_bajra),
                description = getString(R.string.desc_bajra),
                imageResId = R.drawable.grain_bajra,
                badgeText = "Bj",
                badgeColorResId = R.color.bajra_tag,
                botanicalName = "Pennisetum glaucum",
                details = """
                    • Classification standard: Pearl Millet Hybrid / Desi Bajra
                    • Target Moisture Threshold: < 12.0%
                    • Average Diameter: 2.0mm - 3.0mm
                    • Quality Parameters Evaluated: Ergot density percentage, immature green kernels, sand/silica content, seed viability.
                """.trimIndent()
            ),
            SupportedGrain(
                id = 5,
                name = getString(R.string.grain_ragi),
                description = getString(R.string.desc_ragi),
                imageResId = R.drawable.grain_ragi,
                badgeText = "Rg",
                badgeColorResId = R.color.ragi_tag,
                botanicalName = "Eleusine coracana",
                details = """
                    • Classification standard: Finger Millet (Organic Brown / Crimson red)
                    • Target Moisture Threshold: < 11.0%
                    • Average Seed Diameter: 1.5mm - 2.0mm
                    • Quality Parameters Evaluated: Calcium concentration grade, husk retention factor, dust admixture, grain density.
                """.trimIndent()
            )
        )

        val adapter = SupportedGrainsAdapter(grainsList) { grain ->
            showGrainDetails(grain.name, grain.description, grain.details)
        }
        binding.rvSupportedGrains.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSupportedGrains.adapter = adapter
    }

    private fun showGrainDetails(title: String, description: String, details: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage("$description\n\nQuality Standards:\n$details")
            .setPositiveButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_grain_logo)
            .show()
    }

    private fun showAboutProjectDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About Project")
            .setMessage("""
                Project Name:
                Grain Classifier
                
                Technologies:
                • Kotlin
                • TensorFlow Lite
                • Teachable Machine
                • CameraX
                • Room Database
            """.trimIndent())
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_info)
            .show()
    }

    private fun showSettingsDialog() {
        val sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        val isNotificationsEnabled = sharedPrefs.getBoolean("notifications", true)

        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val switchDarkMode = dialogView.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switch_dark_mode)
        val switchNotifications = dialogView.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switch_notifications)

        switchDarkMode.isChecked = isDarkMode
        switchNotifications.isChecked = isNotificationsEnabled

        MaterialAlertDialogBuilder(this)
            .setTitle("Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val editor = sharedPrefs.edit()
                editor.putBoolean("dark_mode", switchDarkMode.isChecked)
                editor.putBoolean("notifications", switchNotifications.isChecked)
                editor.apply()

                if (switchDarkMode.isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_settings)
            .show()
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Help & Instructions")
            .setMessage("""
                Follow these simple steps:
                
                1. Take a Photo
                • Tap "Take Photo" to capture a clear, close-up image of the grains.
                
                2. Upload Image
                • Tap "Upload Image" to select an existing photo from your gallery.
                
                3. View Results
                • Once classified, you will see the grain name, confidence score, botanical name, and safety indices.
                
                4. View History
                • Access "Classification History" from the sidebar menu to see past results.
            """.trimIndent())
            .setPositiveButton("Got It") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_help)
            .show()
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Application")
            .setMessage("Do you want to exit the application?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .setIcon(R.drawable.ic_exit)
            .show()
    }

    private fun triggerCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }
        try {
            val cachePath = File(cacheDir, "grain_images")
            cachePath.mkdirs()
            val file = File(cachePath, "capture_${System.currentTimeMillis()}.jpg")
            
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "com.example.grainclassifier.fileprovider",
                file
            )
            takePictureLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "Camera initialization failed: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun triggerGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun navigateToPrediction(uri: Uri) {
        val intent = Intent(this, PredictionActivity::class.java).apply {
            putExtra("EXTRA_IMAGE_URI", uri.toString())
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Highlight Home when returning to MainActivity
        binding.navView.setCheckedItem(R.id.nav_home)
    }
}
