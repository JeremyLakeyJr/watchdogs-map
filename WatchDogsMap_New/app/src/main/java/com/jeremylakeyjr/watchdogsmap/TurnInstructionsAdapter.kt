package com.jeremylakeyjr.watchdogsmap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jeremylakeyjr.watchdogsmap.databinding.ItemTurnInstructionBinding

class TurnInstructionsAdapter(var instructions: List<RouteStep>) : RecyclerView.Adapter<TurnInstructionsAdapter.ViewHolder>() {

    var highlightedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTurnInstructionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.bind(instruction, position == highlightedPosition)
    }

    override fun getItemCount() = instructions.size

    fun updateInstructions(newInstructions: List<RouteStep>) {
        instructions = newInstructions
        highlightedPosition = 0
        notifyDataSetChanged()
    }

    fun setHighlightedPosition(position: Int) {
        val previousPosition = highlightedPosition
        highlightedPosition = position
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
        if (highlightedPosition != -1) {
            notifyItemChanged(highlightedPosition)
        }
    }

    inner class ViewHolder(private val binding: ItemTurnInstructionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(instruction: RouteStep, isHighlighted: Boolean) {
            binding.instructionText.text = instruction.instruction

            val turnIcon = when {
                instruction.instruction.contains("left", ignoreCase = true) -> R.drawable.ic_turn_left
                instruction.instruction.contains("right", ignoreCase = true) -> R.drawable.ic_turn_right
                else -> R.drawable.ic_turn_straight
            }
            binding.turnIcon.setImageResource(turnIcon)

            if (isHighlighted) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.cyan_transparent))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
            }
        }
    }
}