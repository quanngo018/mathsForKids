package com.example.mathforkids.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * MNIST digit classifier using TensorFlow Lite
 * Recognizes handwritten digits 0-9
 */
class MnistClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var isModelLoaded = false

    companion object {
        private const val TAG = "MnistClassifier"
        private const val MODEL_PATH = "mnist.tflite"
        private const val INPUT_SIZE = 28
        private const val PIXEL_SIZE = 1
        private const val IMAGE_MEAN = 0f
        private const val IMAGE_STD = 255f
        private const val NUM_CLASSES = 10
    }

    init {
        loadModel()
    }

    private fun loadModel() {
        if (isModelLoaded) return

        try {
            val model = loadModelFile(context)

            // Configure interpreter options
            val options = Interpreter.Options()
            options.setNumThreads(4)

            interpreter = Interpreter(model, options)
            isModelLoaded = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load MNIST model", e)
            isModelLoaded = false
        }
    }

    /**
     * Load TFLite model from assets
     */
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Predict digit from bitmap
     * Returns predicted digit (0-9) and confidence (0-1)
     */
    fun classify(bitmap: Bitmap): Pair<Int, Float> {
        // Try to reload model if it failed before
        if (interpreter == null && !isModelLoaded) {
            loadModel()
        }

        if (interpreter == null) {
            Log.e(TAG, "Interpreter is null! Model not loaded.")
            return Pair(-1, 0f)
        }

        // Preprocess bitmap to 28x28 grayscale
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Reset buffer position to 0 before inference
        byteBuffer.rewind()

        // Run inference
        val result = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter?.run(byteBuffer, result)

        // Find digit with highest probability
        val probabilities = result[0]
        var maxIdx = 0
        var maxProb = probabilities[0]

        for (i in 1 until NUM_CLASSES) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIdx = i
            }
        }

        return Pair(maxIdx, maxProb)
    }

    /**
     * Convert bitmap to ByteBuffer for model input
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]

                // Convert to grayscale
                val r = Color.red(value)
                val g = Color.green(value)
                val b = Color.blue(value)
                val grayscale = (r + g + b) / 3f

                // Invert colors: MNIST trained on white digits on black background
                val inverted = 255f - grayscale

                // Normalize to 0-1 range
                byteBuffer.putFloat(inverted / IMAGE_STD)
            }
        }

        return byteBuffer
    }

    /**
     * Close interpreter when done
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
