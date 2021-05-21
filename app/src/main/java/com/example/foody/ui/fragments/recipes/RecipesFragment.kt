package com.example.foody.ui.fragments.recipes

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foody.viewmodels.MainViewModel
import com.example.foody.R
import com.example.foody.adapters.RecipesAdapter
import com.example.foody.databinding.FragmentRecipesBinding
import com.example.foody.util.NetworkListener
import com.example.foody.util.NetworkResult
import com.example.foody.util.observeOnce
import com.example.foody.viewmodels.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RecipesFragment : Fragment(), SearchView.OnQueryTextListener {

    private val args by navArgs<RecipesFragmentArgs>()

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var recipesViewModel: RecipesViewModel
    private val mAdapter by lazy { RecipesAdapter() }

    private lateinit var networkListener: NetworkListener

    //
//    private var nQuieres: HashMap<String, String>? = HashMap()
    private var isPerformingQuery = false

    private lateinit var linearLayoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = linearLayoutManager.findLastVisibleItemPosition()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        recipesViewModel = ViewModelProvider(requireActivity()).get(RecipesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mainViewModel = mainViewModel

        setHasOptionsMenu(true)

        setupRecyclerView()

        recipesViewModel.readBackOnline.observe(viewLifecycleOwner) {
            recipesViewModel.backOnline = it
        }

        //observe searching
//
//        recipesViewModel.readSearch.asLiveData().observe(viewLifecycleOwner) {
//
//            recipesViewModel.searching = it
//            Log.d(
//                "mah RecipesFragment",
//                "isPerformingQuery : " + recipesViewModel.searching.toString()
//            )
//
//        }


        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    Log.d("NetworkListener", status.toString())
                    recipesViewModel.networkStatus = status
                    recipesViewModel.showNetworkStatus()
                    Log.d(
                        "mah RecipesFragment",
                        "searching : " + recipesViewModel.searching.toString()
                    )
                    requestAnyApiData(querySelect())


                }
        }

        binding.recipesFab.setOnClickListener {
            if (recipesViewModel.networkStatus) {

                this.findNavController().navigate(R.id.action_recipesFragment_to_recipesBottomSheet)
            } else {
                recipesViewModel.showNetworkStatus()
            }
        }



        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerview.adapter = mAdapter
        linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.layoutManager = linearLayoutManager
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (!binding.recyclerview.canScrollVertically(1)) {
//                if(totalItemCount == lastVisibleItemPosition+1){
//                    mAdapter.updateData()
                    mainViewModel.nextQuery()
                }

            }
        })

        showShimmerEffect()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipes_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchApiData(query)
        }
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return true
    }

    private fun readDatabase() {
        lifecycleScope.launch {
            mainViewModel.readRecipes.observeOnce(viewLifecycleOwner, Observer { database ->
                if (database.isNotEmpty() && !args.backFromBottomSheet) {
                    Log.d("mah RecipesFragment", "readDatabase called!")
                    mAdapter.setData(database[0].foodRecipe)
                    hideShimmerEffect()
                } else {
                    requestApiData()
                }
            })
        }
    }

    private fun requestApiData() {


        Log.d("mah RecipesFragment", "requestApiData called : " + isPerformingQuery.toString())
        mainViewModel.getRecipes(recipesViewModel.applyQueries())
        mainViewModel.recipesResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    Log.d("mah RecipesFragment", "requestApiData sucsess!")
                    hideShimmerEffect()
                    response.data?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    Log.d(
                        "RecipesFragment",
                        "mah requestApiData error! \n" + response.toString()
                    )
                    hideShimmerEffect()
                    loadDataFromCache()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading -> {
                    Log.d("RecipesFragment", "requestApiData Loading!")
                    showShimmerEffect()
                }
            }
        }

    }

    private fun requestAnyApiData(isSearch: Boolean) {


        Log.d(
            "mah RecipesFragment",
            "quiery is : " + recipesViewModel.applyAnyQuery().toString()
        )
        mainViewModel.getAnyResponse(isSearch, (recipesViewModel.applyAnyQuery()))
        mainViewModel.recipesAnyResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    Log.d("mah RecipesFragment", "requestApiData sucsess!")
                    hideShimmerEffect()
                    response.data?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    Log.d(
                        "RecipesFragment",
                        "mah requestApiData error! \n" + response.toString()
                    )
                    hideShimmerEffect()
                    loadDataFromCache()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading -> {
                    Log.d("RecipesFragment", "requestApiData Loading!")
                    showShimmerEffect()
                }
            }
        }

    }

    private fun searchApiData(searchQuery: String) {
        Log.d("mah RecipesFragment", "searchApiData called")
        showShimmerEffect()
        mainViewModel.applyNextQuery(recipesViewModel.applySearchQuery(searchQuery, 0))
        mainViewModel.searchedRecipesResponse.observeOnce(viewLifecycleOwner, Observer { response ->
            when (response) {
                is NetworkResult.Success -> {
                    Log.d("mah RecipesFragment", "searchApiData sucsess!")
                    hideShimmerEffect()
                    val foodRecipe = response.data
                    foodRecipe?.let { mAdapter.setData(it) }
//                   recipesViewModel.saveSearch(false)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()

//                    Log.d(
//                        "mah RecipesFragment",
//                        "searchApiData applyNextQuery nQuieres  = ! \n" + nQuieres!!.toString() + "$isPerformingQuery"
//                    )
                    Log.d(
                        "mah RecipesFragment",
                        "searchApiData error! \n" + response.message
                    )
                    hideShimmerEffect()
                    // loadDataFromCache()

                }
                is NetworkResult.Loading -> {
                    Log.d("mah RecipesFragment", "searchApiData Loading!")
                    showShimmerEffect()
                }
            }
        })
    }


    private fun loadDataFromCache() {
        lifecycleScope.launch {
            mainViewModel.readRecipes.observe(viewLifecycleOwner) { database ->
                if (database.isNotEmpty()) {
                    mAdapter.setData(database[0].foodRecipe)
                }
            }
        }
    }

//    private fun readDatabase() {
//        lifecycleScope.launch {
//            mainViewModel.readRecipes.observeOnce(viewLifecycleOwner, Observer { database ->
//                if (database.isNotEmpty() && !args.backFromBottomSheet) {
//                    Log.d("mah RecipesFragment", "readDatabase called!")
//                    mAdapter.setData(database[0].foodRecipe)
//                    hideShimmerEffect()
//                } else {
//                    requestApiData()
//                }
//            })
//        }
//    }

    private fun querySelect(): Boolean {
        val query = recipesViewModel.applyAnyQuery()
        if (query.containsKey("category")) {
            val category = query["category"]
            val cuisine = query["cuisine"]
            return !(category.equals("") && cuisine.equals(""))

            return true
        }
        recipesViewModel.saveSearch(false)
        return false
    }


    private fun showShimmerEffect() {
        binding.recyclerview.showShimmer()
    }

    private fun hideShimmerEffect() {
        binding.recyclerview.hideShimmer()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}