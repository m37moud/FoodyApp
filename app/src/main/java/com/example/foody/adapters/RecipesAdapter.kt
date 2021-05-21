package com.example.foody.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foody.databinding.RecipesRowLayoutBinding
import com.example.foody.models.FoodRecipe
import com.example.foody.models.Recipe
import com.example.foody.util.RecipesDiffUtil

class RecipesAdapter : RecyclerView.Adapter<RecipesAdapter.MyViewHolder>() {

    private var recipes = emptyList<Recipe>()
    private var recipes2 = emptyList<Recipe>()



    class MyViewHolder(private val binding: RecipesRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(result: Recipe){
            binding.result = result

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentRecipe = recipes[position]

        holder.bind(currentRecipe)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    fun setData(newData: FoodRecipe){
//        val recipesDiffUtil =
//            RecipesDiffUtil(recipes, newData.recipes)
//        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)

            recipes = newData.recipes


//        diffUtilResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    fun updateData(newData: FoodRecipe){
        var list = recipes.toMutableList()
        list.addAll(newData.recipes)
        recipes = list
        notifyDataSetChanged()

    }



}