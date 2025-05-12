package com.alya.ecommerce_serang.ui.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.response.customer.product.ReviewsItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ReviewsAdapter(
    private var reviewList: List<ReviewsItem>
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        with(holder) {
            tvReviewerName.text = review.username
            tvReviewRating.text = review.rating.toString()
            tvReviewText.text = review.reviewText
            tvDateReview.text = formatDate(review.reviewDate)
        }
    }

    override fun getItemCount(): Int = reviewList.size

    fun setReviews(reviews: List<ReviewsItem>) {
        reviewList = reviews
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()) //from json
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") //get timezone
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // new format
            val date = inputFormat.parse(dateString)  // Parse from json format
            outputFormat.format(date!!)  // convert to new format
        } catch (e: Exception) {
            dateString // Return original if error occurs
        }
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewRating: TextView = itemView.findViewById(R.id.tvReviewRating)
        val tvReviewerName: TextView = itemView.findViewById(R.id.tvUsername)
        val tvReviewText: TextView = itemView.findViewById(R.id.tvReviewText)
        val tvDateReview: TextView = itemView.findViewById(R.id.date_review)
    }
}