package com.calpito.mediaplayer.ui.fragments



import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ThemedSpinnerAdapter.Helper
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calpito.mediaplayer.R
import com.calpito.mediaplayer.databinding.FragmentPlaylistBinding
import com.calpito.mediaplayer.model.PlayListItem
import com.calpito.mediaplayer.model.Song
import com.calpito.mediaplayer.utility.HelperFunctions
import com.calpito.mediaplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class PlayListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentPlaylistBinding
    
    private lateinit var adapter: MyAdapter


    /*set up data the fragment needs*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    /*bind View*/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }


    /*Views are ready to be set up*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setObservers()
    }


    fun initView() {

        //Back Button
        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }


        //init RecyclerView
        //VERTICAL RECYCLER
        binding.rvMain.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter(onItemClicked = ::listItemClicked)


        binding.rvMain.adapter = adapter





        //now lets collect our ui states so we can update our ui as required
        lifecycleScope.launch {
            // This will make sure the block is cancelled and restarted according to the lifecycle changes
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.getMusicPlayerStateFlow().collect {
                   //todo update ui with data that we have collected
                }
            }
        }

    }

    private fun setObservers() {
        //now lets collect our ui states so we can update our ui as required
        lifecycleScope.launch {
            // This will make sure the block is cancelled and restarted according to the lifecycle changes
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.getMusicPlayerStateFlow().collect {
                    Log.d("MEDIA_PLAYER_UPDATE", it.toString())
                    val songItems = it.songList.map { song->PlayListItem.SongItem(song) }
                    adapter.submitList(songItems)
                }
            }
        }
    }


    /*Adapter which supports different types of items in the list.*/
    class MyAdapter(private val onItemClicked: (PlayListItem) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            //we are going to have 1 text views to specify the Letter of the states below this header
            val tvTitle: TextView? = itemView.findViewById(R.id.tv_title)
            val tvArtist: TextView? = itemView.findViewById(R.id.tv_artist)
            val tvLength: TextView? = itemView.findViewById(R.id.tv_song_length)


            //function to set data to the textView
            fun bind(data: Song) {
                tvTitle?.text = data.title
                tvArtist?.text = data.artist
                tvLength?.text = HelperFunctions.convertMillisToMMSS(data.durationMs)


                //click listener for this item
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        println("CLICKED ITEM: ${differ.currentList[position]}, ${data.toString()}")
                        onItemClicked(differ.currentList[position])
                    }
                }
            }
        }
        //todo create any other view holders here
        override fun getItemCount(): Int {
            return differ.currentList.size
        }

        override fun getItemViewType(position: Int): Int {
            var result = 0
            when(differ.currentList[position]){
                is PlayListItem.SongItem->{
                    result = R.layout.playlist_item
                }
                //add anymore types here
                else ->{
                    //default view type if we have any
                }
            }

            return result //just return the position, because when creating the view holder, the viewType is basically imbedded in the object in the list.
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //inflate a view depending on what type of item we have. Since our items are typed by sealed class, we just go based on the actual item we are goign to display

            val viewHolder = when (viewType) {
                R.layout.playlist_item -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.playlist_item, parent, false)
                    SongViewHolder(view)
                }

               else -> {
                   //invalid viewType, we cannot create a view holder
                   throw Exception("invalid viewType")
               }
            }

            return viewHolder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val playListItem = differ.currentList[position]

            //depending on what type of holder we have, we have a different bind function with certain params
            when (holder) {
                is SongViewHolder -> {
                    if (playListItem is PlayListItem.SongItem) { //make sure our data is correct for a SongViewHolder
                        holder.bind(playListItem.song)
                    }
                }

               else->{
                   //unknown viewHolder, do not know how to bind
               }

            }

        }


        /*this code sets up a mechanism for efficiently handling updates to a list of
        objects in a RecyclerView. It uses DiffUtil for calculating differences between lists,
        and AsyncListDiffer for handling these differences asynchronously and efficiently
        updating the UI. The submitList method is a convenient way to submit a
        new list for calculation and update.*/
        private val diffCallback = object : DiffUtil.ItemCallback<PlayListItem>() {
            override fun areItemsTheSame(oldItem: PlayListItem, newItem: PlayListItem): Boolean {
                var result = false
                result = when {
                    oldItem is PlayListItem.SongItem && newItem is PlayListItem.SongItem -> {
                        oldItem.song.uuid == newItem.song.uuid
                    }

                    //todo add anymore different items here

                    else -> {
                        false
                    }
                }

                return result
            }

            override fun areContentsTheSame(oldItem: PlayListItem, newItem: PlayListItem): Boolean {
                var result = false
                result = when {
                    oldItem is PlayListItem.SongItem && newItem is PlayListItem.SongItem -> {
                        oldItem.song == newItem.song
                    }

                    //todo add any ore items here

                    else -> {
                        false
                    }
                }

                return result
            }
        }

        private val differ = AsyncListDiffer(this, diffCallback)

        fun submitList(list: List<PlayListItem>) = differ.submitList(list)
    }

    /*handles item clicks in the recycler view*/
    fun listItemClicked(clickedItem: PlayListItem) {
        when(clickedItem){
            is PlayListItem.SongItem -> {
                mainViewModel.songSelected(clickedItem.song)
                findNavController().popBackStack()
            }

            //todo handle any other types of clicked items
        }


    }
}