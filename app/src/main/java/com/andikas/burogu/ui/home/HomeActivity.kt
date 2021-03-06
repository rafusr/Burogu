package com.andikas.burogu.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.andikas.burogu.R
import com.andikas.burogu.adapter.ArticleAdapter
import com.andikas.burogu.data.IdentifySharedPref
import com.andikas.burogu.data.local.ArticleDatabase
import com.andikas.burogu.data.model.Article
import com.andikas.burogu.databinding.ActivityHomeBinding
import com.andikas.burogu.ui.detail.DetailActivity
import com.andikas.burogu.ui.edit.EditActivity
import com.andikas.burogu.ui.profile.ProfileActivity
import com.andikas.burogu.utils.Extensions.hideActionBar
import com.andikas.burogu.utils.Extensions.navigateTo
import kotlinx.coroutines.Dispatchers

class HomeActivity : AppCompatActivity(), HomeView {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreference: IdentifySharedPref
    private lateinit var presenter: HomePresenterImp
    private var isGrid = false
    private var database: ArticleDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideActionBar()

        sharedPreference = IdentifySharedPref(this)
        database = ArticleDatabase.getInstance(this)
        presenter = HomePresenterImp(database!!, this, Dispatchers.Main)

        presenter.insertDummyArticles()

        binding.txtUsername.text = sharedPreference.userName
        binding.btnLayoutManager.setOnClickListener {
            isGrid = !isGrid
            presenter.loadAllArticles()
        }

        binding.btnProfile.setOnClickListener { navigateTo(ProfileActivity::class.java) }
        binding.btnAdd.setOnClickListener { navigateTo(EditActivity::class.java) }
    }

    override fun showAllArticles(articles: List<Article>) {
        val articleAdapter = ArticleAdapter(articles, {
            navigateTo(DetailActivity::class.java, "extraArticle", it)
        }, {
            presenter.setArticleBookmark(it, !it.isBookmarked)
        })

        if (isGrid) {
            binding.btnLayoutManager.setImageResource(R.drawable.ic_list)
            binding.rvArticle.apply {
                layoutManager = GridLayoutManager(this@HomeActivity, 2)
                setHasFixedSize(true)
                adapter = articleAdapter
            }
        } else {
            binding.btnLayoutManager.setImageResource(R.drawable.ic_grid)
            binding.rvArticle.apply {
                layoutManager = LinearLayoutManager(this@HomeActivity)
                setHasFixedSize(true)
                adapter = articleAdapter
            }
        }
    }

    override fun refreshArticles() {
        presenter.loadAllArticles()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadAllArticles()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}