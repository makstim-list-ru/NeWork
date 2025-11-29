package ru.netology.nework.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.UserCardBinding
import ru.netology.nework.ui.retrofit.User

class UsersAdapter(
    private val callback: (User) -> Unit,
) :
    PagingDataAdapter<User, RecyclerView.ViewHolder>(UserDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = UserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return UserViewHolder(binding, callback)
    }

    override fun getItemViewType(position: Int) = R.layout.user_card

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is User -> (holder as UserViewHolder).onBindUser(item)
            else -> error("unknown item type")
        }
    }
}

object UserDiffUtil : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: User,
        newItem: User
    ): Boolean {
        return oldItem == newItem
    }
}

class UserViewHolder(
    private val binding: UserCardBinding,
    private val callback: (User) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun onBindUser(user: User) {


        with(binding) {
            userName.text = user.name
            userLogin.text = user.login

            Glide.with(userAvatar).clear(userAvatar)
            binding.userAvatar.visibility = View.INVISIBLE
            val url = user.avatar
            url?.let {
                Glide.with(binding.userAvatar)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.userAvatar)
                binding.userAvatar.visibility = View.VISIBLE
            }
        }
    }
}



