package ru.netology.nework.ui.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.ui.auth.AuthApp
import ru.netology.nework.ui.retrofit.AttachmentType
import ru.netology.nework.ui.retrofit.Post
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

enum class KeyPostViewHolder { VIDEO, LIKE, SHARE, POST, REMOVE, EDIT, CANCEL }

class PostsAdapter(
    private val callback: (Post, KeyPostViewHolder) -> Unit,
    private val authApp: AuthApp
) :
    PagingDataAdapter<Post, RecyclerView.ViewHolder>(PostDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostViewHolder(binding, callback, authApp)
    }

    override fun getItemViewType(position: Int) = R.layout.post_card

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is Post -> (holder as PostViewHolder).onBindPost(item)
            else -> error("unknown item type")
        }
    }
}

object PostDiffUtil : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem == newItem
    }
}

class PostViewHolder(
    private val binding: PostCardBinding,
    private val callback: (Post, KeyPostViewHolder) -> Unit,
    private val authApp: AuthApp
) :
    RecyclerView.ViewHolder(binding.root) {
    fun onBindPost(post: Post) {

        val flagAtt = post.attachment?.let {
            when (post.attachment.type) {
                AttachmentType.IMAGE -> "IM"
                AttachmentType.VIDEO -> "VI"
                AttachmentType.AUDIO -> "AU"
            }
        } ?: "00"

        with(binding) {
            postAuthor.text = String.format(
                Locale.getDefault(),
                "%s ID=%d aID=%d %s",
                post.author,
                post.id, post.authorId,
                flagAtt
            )

            postPublished.text = getFormattedDateTime(post.published)
            postContent.text = post.content

            val numLikes = post.likeOwnerIds?.size?.toLong() ?: 0L
            postLike.text = getFormatedNumber(numLikes)
            postLike.isChecked = post.likedByMe

            postShare.text = "" //getFormatedNumber(post.sharesNum)

            postViewsNumber.text = getFormatedNumber(post.seenNum)

            postLike.setOnClickListener {
                if (!authApp.authenticated) {
                    postLike.isChecked = false
                    Toast.makeText(
                        postLike.context,
                        "You need to be authorized to LIKE posts. Please, authorize...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else
                    callback(post, KeyPostViewHolder.LIKE)
            }

            postShare.setOnClickListener {
                callback(post, KeyPostViewHolder.SHARE)
            }

            postAuthor.setOnClickListener {
                callback(post, KeyPostViewHolder.POST)
            }
            postPublished.setOnClickListener {
                callback(post, KeyPostViewHolder.POST)
            }
            postContent.setOnClickListener {
                callback(post, KeyPostViewHolder.POST)
            }
            postMediaBox.setOnClickListener {
                callback(post, KeyPostViewHolder.POST)
            }

            postMediaBoxVideo.setOnClickListener {
                //todo
            }

            postThreeDotsMenu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            postThreeDotsMenu.setOnClickListener { view ->
                val pum = PopupMenu(view.context, view)
                pum.inflate(R.menu.menu_options)
                pum.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.remove -> {
                            callback(post, KeyPostViewHolder.REMOVE)
                            true
                        }

                        R.id.edit -> {
                            callback(post, KeyPostViewHolder.EDIT)
                            true
                        }

                        else -> false
                    }
                }
                pum.show()
            }

            val url = post.authorAvatar
            url?.let {
                binding.postAvatar.visibility = View.VISIBLE
                Glide.with(binding.postAvatar)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.postAvatar)
            } ?: {
                Glide.with(postAvatar).clear(postAvatar)
                binding.postAvatar.visibility = View.INVISIBLE
            }

            Glide.with(postMediaBox).clear(postMediaBox)
            postMediaBox.visibility = View.GONE

            postMediaBoxVideo.player?.release()
            postMediaBoxVideo.player = null
            postMediaBoxVideo.visibility = View.GONE

            postMediaBoxAudio.player?.release()
            postMediaBoxAudio.player = null
            postMediaBoxAudio.visibility = View.GONE

            post.attachment?.let {
                if (URLUtil.isValidUrl(post.attachment.url))
                    when (post.attachment.type) {

                        AttachmentType.IMAGE -> {

                            val urlMedia = post.attachment.url
                            Glide.with(postMediaBox)
                                .load(urlMedia)
                                .placeholder(R.drawable.ic_loading_100dp)
                                .error(R.drawable.ic_error_100dp)
                                .timeout(10_000)
                                .into(postMediaBox)

                            postMediaBox.visibility = View.VISIBLE
                        }

                        AttachmentType.VIDEO -> {

                            val urlMediaVideo = post.attachment.url

                            val player = ExoPlayer.Builder(postMediaBoxVideo.context)
                                .build()
                            postMediaBoxVideo.player = player
                            val mediaItem = MediaItem.fromUri(urlMediaVideo)
                            player.setMediaItem(mediaItem)
//                            player.playWhenReady = true
//                            player.volume = 0F
                            player.prepare()

                            postMediaBoxVideo.visibility = View.VISIBLE
                        }

                        AttachmentType.AUDIO -> {

                            val urlMediaVideo = post.attachment.url

                            val player = ExoPlayer.Builder(postMediaBoxAudio.context)
                                .build()
                            postMediaBoxAudio.player = player
                            val mediaItem = MediaItem.fromUri(urlMediaVideo)
                            player.setMediaItem(mediaItem)
//                            player.playWhenReady = true
//                            player.volume = 0F
                            player.prepare()

                            postMediaBoxAudio.visibility = View.VISIBLE
                        }
                    }
            }

//            post.attachment?.let {
//                postMediaBox.visibility = View.VISIBLE
//                val urlMedia = post.attachment.url
//                Glide.with(postMediaBox)
//                    .load(urlMedia)
//                    .placeholder(R.drawable.ic_loading_100dp)
//                    .error(R.drawable.ic_error_100dp)
//                    .timeout(10_000)
//                    .into(postMediaBox)
//            } ?: {
//                Glide.with(postMediaBox).clear(postMediaBox)
//                postMediaBox.visibility = View.GONE
//            }
        }
    }

    private fun getFormatedNumber(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        return if (count / 1000.0.pow(exp.toDouble()) >= 10) {
            String.format(
                Locale.getDefault(),
                "%.0f%c",
                count / 1000.0.pow(exp.toDouble()),
                "kMGTPE"[exp - 1]
            )
        } else {
            String.format(
                Locale.getDefault(),
                "%.1f%c",
                floor(count / 1000.0.pow(exp.toDouble()) * 10) / 10,
                "kMGTPE"[exp - 1]
            )
        }
    }

    private fun getFormattedDateTime(stringIn: String): String {

        if (stringIn.isEmpty()) return ""

        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val parsedDate = formatter.parse(stringIn)
        val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:MM")
        val stringOut = displayFormatter.format(parsedDate)

        return stringOut
    }
}



