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


    // viewModel for communication with database
    private val viewModel: RGBViewModel by viewModels {
        RGBViewModelFactory((application as Application).repository)
    }

    // launcher for requesting permission
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        var granted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                granted = false
        }

        if (granted)
            startCamera()
        else
            Toast.makeText(baseContext, "Required permissions denied", Toast.LENGTH_SHORT).show()
    }

    // analyzer use-case
    private val analyzer = RGBAnalyzer { r, g, b ->

        /**per modificare il tempo limite, basta cambiare il valore di questa variabile*/
        TIME_LIMIT = System.currentTimeMillis() - 5 * 60 * 1000

        // format the data
        val red  = floor(r.toFloat() * 1000) / 1000
        val green = floor(g.toFloat() * 1000) / 1000
        val blue = floor(b.toFloat() * 1000) / 1000

        // pass the data to the database
        val color = Color.valueOf(red, green, blue)
        val rgb = RGB(System.currentTimeMillis(), color)
        viewModel.insert(rgb)

        // delete data inserted before the time_limit delta
        viewModel.deleteOldData(TIME_LIMIT)

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

        if (allPermissionGranted())
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

    private fun showArchive() {

        /*// set up the RecyclerView
        val recyclerView: RecyclerView = binding.recyclerView
        val adapter = RGBListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)*/

        recyclerView.visibility = View.VISIBLE

        // setting ViewModel's observer
        viewModel.allData.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    /** check if all permissions are granted */
    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /** ask the user for required permissions */
    private fun requirePermissions() {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }


    companion object {
        const val TAG = "Color Analyzer"
        const val THREAD_EXECUTION_TIME = 1000L
        private var TIME_LIMIT = 0L
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

}