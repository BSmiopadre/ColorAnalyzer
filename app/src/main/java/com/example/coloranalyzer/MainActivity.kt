package com.example.coloranalyzer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coloranalyzer.database.RGB
import com.example.coloranalyzer.database.RGBListAdapter
import com.example.coloranalyzer.database.RGBViewModel
import com.example.coloranalyzer.database.RGBViewModelFactory
import com.example.coloranalyzer.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RGBListAdapter
    private lateinit var cameraExecutor: ExecutorService
    private var onShowArchive = false

    /** viewModel for communication with database */
    private val viewModel: RGBViewModel by viewModels {
        RGBViewModelFactory((application as Application).repository)
    }

    /** launcher for requesting permission */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        // flag
        var granted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                granted = false
        }

        // if all permissions in the list are granted, then start the camera
        if (granted)
            startCamera()
        else
            Toast.makeText(baseContext, "Required permissions denied", Toast.LENGTH_SHORT).show()
    }

    /** analyzer use-case */
    private val analyzer = RGBAnalyzer { r, g, b ->

        // format the data
        val red  = floor(r.toFloat() * 1000) / 1000
        val green = floor(g.toFloat() * 1000) / 1000
        val blue = floor(b.toFloat() * 1000) / 1000

        // pass the data to the database
        val color = Color.valueOf(red, green, blue)
        val rgb = RGB(System.currentTimeMillis(), color)
        viewModel.insert(rgb)

        // delete data inserted before the time_limit delta
        val timeLimit = System.currentTimeMillis() - TIME_LIMIT
        viewModel.deleteOldData(timeLimit)

        // show the data on the screen
        runOnUiThread {

            var msg = "Average R:  $red"
            binding.averageRed.text = msg
            Log.d(TAG, msg)

            msg = "Average G:  $green"
            binding.averageGreen.text = msg
            Log.d(TAG, msg)

            msg = "Average B:  $blue"
            binding.averageBlue.text = msg
            Log.d(TAG, msg)
        }

        // execute every second
        Thread.sleep(THREAD_EXECUTION_TIME)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up the button to display data archive
        val button = binding.archiveButton
        button.setOnClickListener {
            showArchive()
            onShowArchive = true
        }

        // set up the RecyclerView
        recyclerView = binding.recyclerView
        adapter = RGBListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        if (savedInstanceState != null) {
            onShowArchive = savedInstanceState.getBoolean("onShowArchive")
            if (onShowArchive)
                showArchive()
        }

        if (allPermissionsGranted())
            startCamera()
        else
            requirePermissions()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (onShowArchive) {

            binding.recyclerView.visibility = View.INVISIBLE
            onShowArchive = false
        }
        else
            super.onBackPressed()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("onShowArchive", onShowArchive)
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    /** bind the necessaries use cases to the Camera to start performing all main tasks */
    private fun startCamera() {

        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener({
            // get the process camera provider
            val cameraProvider = processCameraProvider.get()

            // set up the preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            // set up the analyzer
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, analyzer)
                }

            // set the back camera as default camera to open on app start
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // unbind all use cases
                cameraProvider.unbindAll()
                // bind required use cases
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            } catch (e: Exception) {
                Toast.makeText(baseContext, "Use cases binding failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    /** display the archive containing the data stored in the database */
    private fun showArchive() {

        recyclerView.visibility = View.VISIBLE

        // setting ViewModel's observer
        viewModel.allData.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    /** check if all required permissions are granted */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /** ask the user for required permissions */
    private fun requirePermissions() {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }


    companion object {
        const val TAG = "Color Analyzer"
        private const val THREAD_EXECUTION_TIME = 1000L
        private const val TIME_LIMIT: Long = 5 * 60 * 1000    /** per modificare il tempo limite di permanenza nel db, modificare il valore di questa variabile */
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

}