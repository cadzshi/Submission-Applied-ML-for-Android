package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.data.local.db.HistoryResult
import com.dicoding.asclepius.data.local.repository.HistoryResultRepository
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.ViewModelFactory
import com.dicoding.asclepius.viewmodel.ResultViewModel
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var historyResultRepository: HistoryResultRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyResultRepository = HistoryResultRepository(application)
        resultViewModel = obtainViewModel(this@ResultActivity)

        setTitle()

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
        }
        startClassify(imageUri)

    }

    private fun startClassify(imageUri: Uri) {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@ResultActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {

                                println(it)
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it?.score }

                                val label = sortedCategories[0].label
                                val score = sortedCategories[0].score.toString()
                                val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI)).toString()
                                val historyResult = HistoryResult(image = imageUri, label = label, score = score)
                                resultViewModel.insertBookmarkResult(historyResult)

                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${it.label} " + NumberFormat.getPercentInstance()
                                            .format(it.score).trim()
                                    }
                                binding.resultText.text = displayResult
                            } else {
                                binding.resultText.text = ""
                            }
                        }
                    }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(imageUri)
    }

    private fun setTitle(){
        val title = getString(R.string.result)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun obtainViewModel(activity: AppCompatActivity): ResultViewModel {
        historyResultRepository = HistoryResultRepository(application)
        val factory = ViewModelFactory.getInstance(historyResultRepository)
        return ViewModelProvider(activity, factory)[ResultViewModel::class.java]
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }

}

