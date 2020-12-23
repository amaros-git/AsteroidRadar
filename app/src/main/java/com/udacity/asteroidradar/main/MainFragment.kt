package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import timber.log.Timber


class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, MainViewModel.Factory(activity.application))
            .get(MainViewModel::class.java)
    }

    private lateinit var adapter: AsteroidRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.app_name)

        val binding = FragmentMainBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        adapter = AsteroidRecyclerAdapter(AsteroidClickListener {
            viewModel.displayAsteroidDetails(it)
        })
        binding.asteroidRecycler.adapter = adapter

        viewModel.refreshRecycler()

        viewModel.todayAsteroids.observe(viewLifecycleOwner) {
            it?.let {
                Timber.i("Today")
                refreshAdapter(it)
                adapter.submitMyList(it, false)
                viewModel.clearTodayAsteroidsData()
            }

        }
        viewModel.weekAsteroids.observe(viewLifecycleOwner) {
            it?.let{
                Timber.i("Week")
                refreshAdapter(it)
                adapter.submitMyList(it, false)
                viewModel.clearWeekAsteroidsData()
            }
        }
        viewModel.allAsteroids.observe(viewLifecycleOwner) {
            it?.let {
                Timber.i("All")
                refreshAdapter(it)
                adapter.submitMyList(it, false)
                viewModel.clearAllAsteroidsData()
            }
        }

        viewModel.navigateToDetailsEvent.observe(viewLifecycleOwner) {
            it?.let {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsCompleted()
            }
        }

        viewModel.showToastEvent.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.showToastEventCompleted()
            }
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_today_asteroids -> viewModel.getTodayAsteroids()
            R.id.show_week_asteroids -> viewModel.getWeekAsteroids()
            else -> viewModel.getAllAsteroids()
        }
        return true
    }

    private fun refreshAdapter(list: List<Asteroid>) {
        for (i in list.indices) {
            adapter.notifyItemRemoved(i)
        }
    }

    /*fun getBitmap(context: Context, url: String?): Bitmap? {
        val CACHE_PATH: String =
            context.cacheDir.absolutePath.toString() + "/picasso-cache/"
        val files: Array<File?> = File(CACHE_PATH).listFiles()
        for (file in files) {
            val fname: String = file!!.name
            if (fname.contains(".") && fname.substring(fname.lastIndexOf(".")) == ".0") {
                try {
                    val br = BufferedReader(FileReader(file))
                    if (br.readLine().equals(url)) {
                        val image_path = CACHE_PATH + fname.replace(".0", ".1")
                        if (File(image_path).exists()) {
                            return BitmapFactory.decodeFile(image_path)
                        }
                    }
                } catch (e: FileNotFoundException) {
                } catch (e: IOException) {
                }
            }
        }
        return null
    }*/
}
