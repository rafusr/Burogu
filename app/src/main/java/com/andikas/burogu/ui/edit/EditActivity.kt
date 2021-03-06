package com.andikas.burogu.ui.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.andikas.burogu.data.IdentifySharedPref
import com.andikas.burogu.data.local.ArticleDatabase
import com.andikas.burogu.data.model.Article
import com.andikas.burogu.databinding.ActivityEditBinding
import com.andikas.burogu.utils.Extensions.hideActionBar
import com.andikas.burogu.utils.Extensions.makeToast
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity(), EditView {
    private lateinit var binding: ActivityEditBinding
    private lateinit var sharedPreference: IdentifySharedPref
    private lateinit var presenter: EditPresenterImp
    private lateinit var articleInputArray: Array<EditText>
    private var imagePath: String? = null
    private var database: ArticleDatabase? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val selectedImage: Uri? = it.data?.data
                imagePath = selectedImage.toString()
                binding.edtContentImage.setText(selectedImage?.lastPathSegment)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideActionBar()

        val extraArticle = intent.getParcelableExtra<Article>("extraArticle")

        sharedPreference = IdentifySharedPref(this)
        database = ArticleDatabase.getInstance(this)
        presenter = EditPresenterImp(database!!, this, Dispatchers.Main)
        articleInputArray = arrayOf(
            binding.edtContentTitle,
            binding.edtContent,
            binding.edtContentAuthor
        )

        updateArticle(extraArticle)
        binding.edtContentAuthor.setText(sharedPreference.userName)
        binding.btnContentImage.setOnClickListener { presenter.getImage() }
        binding.btnSubmit.setOnClickListener { submitArticle(false) }
        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    override fun getContentImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_OPEN_DOCUMENT

        getContent.launch(intent)
    }

    override fun submitArticle(isUpdate: Boolean) {
        val notEmpty: Boolean = binding.edtContentTitle.text.toString()
            .isNotEmpty() && binding.edtContent.text.toString()
            .isNotEmpty() && binding.edtContentAuthor.text.toString().isNotEmpty()

        val title = binding.edtContentTitle.text.toString()
        val content = binding.edtContent.text.toString()
        val image = imagePath
        val author = binding.edtContentAuthor.text.toString()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val createdDate = dateFormat.format(Date())

        if (isUpdate) {
            val article = Article(
                id = null,
                imagePath = image,
                title = title,
                content = content,
                author = author
            )

            presenter.updateArticle(article)
        } else {
            val article = Article(
                id = null,
                imagePath = image,
                title = title,
                content = content,
                createdDate = createdDate,
                author = author
            )

            if (notEmpty) {
                presenter.insertArticle(article)
            } else {
                articleInputArray.forEach { input ->
                    if (input.text.toString().isEmpty()) {
                        input.error = "${input.hint} tidak boleh kosong"
                    }
                }
            }
        }
    }

    override fun updateArticle(article: Article?) {
        if (article != null) {
            imagePath = article.imagePath.toString()

            if (article.isDummy) {
                binding.edtContentImage.setText(article.imagePath)
            } else {
                if (article.imagePath != null) {
                    binding.edtContentImage.setText(Uri.parse(article.imagePath).lastPathSegment)
                } else {
                    binding.edtContentImage.text = article.imagePath
                }
            }

            binding.edtContentTitle.setText(article.title)
            binding.edtContent.setText(article.content)
            binding.edtContentAuthor.setText(article.author)

            binding.btnSubmit.setOnClickListener {
                submitArticle(true)
            }
        }
    }

    override fun showMessage(msg: String) {
        makeToast(msg)
    }

    override fun closeScreen() {
        finish()
    }
}