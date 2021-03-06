package com.udacity.asteroidradar.detail


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentDetailBinding


class DetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailBinding.inflate(inflater)

        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.detail_fragment_title)

        binding.lifecycleOwner = this

        val asteroid = DetailFragmentArgs.fromBundle(arguments!!).selectedAsteroid

        binding.asteroid = asteroid

        binding.helpButton.setOnClickListener {
            displayAstronomicalUnitExplanationDialog()
        }

        return binding.root
    }

    private fun displayAstronomicalUnitExplanationDialog() {
        val builder = AlertDialog.Builder(activity!!)
            .setMessage(getString(R.string.astronomica_unit_explanation))
            .setPositiveButton(R.string.OK_button_text, null)
        builder.create().show()
    }
}
