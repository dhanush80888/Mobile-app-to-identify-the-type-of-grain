package com.example.grainclassifier

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.grainclassifier.ml.GrainClassifier
import java.util.concurrent.ExecutorService

/**
 * Dedicated frame processing analyzer class to continuously capture camera frames,
 * convert them, and execute local TensorFlow Lite classifications in a background thread.
 */
class GrainImageAnalyzer(
    private val classifier: GrainClassifier,
    private val executorService: ExecutorService,
    private val onPredictionResult: (GrainClassifier.Prediction) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L
    private val analysisIntervalMs = 400L // Analyze every 400 milliseconds to avoid overloading and preserve battery

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp < analysisIntervalMs) {
            image.close()
            return
        }

        lastAnalyzedTimestamp = currentTimestamp

        // Submit the heavy tasks to background executor service to prevent blocking the analyzer thread
        executorService.execute {
            try {
                // 1. Convert ImageProxy to Bitmap (Uses highly optimized CameraX 1.3.0 native member)
                val bitmap = image.toBitmap()
                // 2. Rotate bitmap based on camera frame rotation degree
                val rotatedBitmap = rotateBitmap(bitmap, image.imageInfo.rotationDegrees)
                
                // 3. Perform TFLite inference
                val prediction = classifier.classifyImage(rotatedBitmap)
                
                // 4. Return results back to caller
                onPredictionResult(prediction)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // CRITICAL: Always close the ImageProxy to prevent memory leaks and frame freezes
                image.close()
            }
        }
    }

    /**
     * Rotation support function using standard Matrix operations
     */
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
