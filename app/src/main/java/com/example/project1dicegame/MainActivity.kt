package com.example.project1dicegame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.project1dicegame.databinding.ActivityMainBinding
import kotlin.random.Random

// Main activity of the app, inheriting from AppCompatActivity.
class MainActivity : AppCompatActivity() {

    // Late-initialize the binding object for activity_main.xml.
    private lateinit var binding: ActivityMainBinding

    private lateinit var player1: Player
    private lateinit var player2: Player
    private lateinit var currentPlayer: Player

    private var shouldRollAgain = false
    private var doublePointsNextRoll = false
    private var jackpot = 5
    private var lastDiceRollResult: Int = 0

    var correctAnswer: Int = 0


    // Called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player1 = Player("Player 1")
        player2 = Player("Player 2")
        currentPlayer = player1 // Start with player 1

        // Update the UI to show the current player's turn as soon as the app starts
        binding.tvCurrentPlayer.text = "${currentPlayer.name}"
        binding.tvJackpotTotal.text = "$jackpot"
        binding.tvPlayer1Total.text = "0"
        binding.tvPlayer2Total.text = "0"

        // Set an onClickListener to the button, calling rollDice() when clicked.
        binding.btnRoll.setOnClickListener {
            binding.tvUserNotification.text = null
            rollDice()
            binding.etUserAnswer.text = null

            //In the event player rolls a 5
            if (lastDiceRollResult == 5){
                binding.tvEquation.text = null
                binding.tvUserNotification.text = "${currentPlayer.name}'s turn now! Roll Dice to continue."
            }
        }

        //OnClickListener to the enter button calling validateAnswer() when clicked
        binding.btnEnter.setOnClickListener {
            val userAnswer = binding.etUserAnswer.text.toString().toIntOrNull()
            userNotifications(userAnswer) // Pass the user's answer for notification
        }
    }


    //Function to roll dice
    private fun rollDice() {
        val dice = Dice(6)
        lastDiceRollResult = dice.diceRoll() // Roll the dice and store the result

        // Handle the action based on the dice roll
        when (lastDiceRollResult) {
            1 -> generateAdditionProblem()
            2 -> generateSubtractionProblem()
            3, 6 -> generateMultiplicationProblem()
            4 -> {
                // Flag that a reroll is needed for double points
                shouldRollAgain = true
                doublePointsNextRoll = true
                binding.tvEquation.text = null
                binding.tvUserNotification.text = "${currentPlayer.name} rolls a 4, roll again for double points!"
            }
            5 -> switchTurns()
        }
        //when expression to determine the drawable based on the dice roll
        val drawableResource = when (lastDiceRollResult) {
            1 -> R.drawable.perspective_dice_six_faces_one
            2 -> R.drawable.perspective_dice_six_faces_two
            3 -> R.drawable.perspective_dice_six_faces_three
            4 -> R.drawable.perspective_dice_six_faces_four
            5 -> R.drawable.perspective_dice_six_faces_five
            else -> R.drawable.dice_six // Default case for 6 and any other unexpected value
        }
        // Update the ImageView (dice) with the chosen drawable
        binding.imageview.setImageResource(drawableResource)
    }


    //Function which generates an addition problem
    private fun generateAdditionProblem(): String {
        // Generating two random numbers from 0 and 99
        val number1 = Random.nextInt(0, 99)
        val number2 = Random.nextInt(0, 99)

        // Storing the correct answer
        correctAnswer = number1 + number2

        // Create an addition problem with these numbers
        val problem = "$number1 + $number2"

        binding.tvEquation.text = problem

        return problem
    }

    //Function which generates a subtraction problem
    private fun generateSubtractionProblem(): String {
        // Generating two random numbers from 0 and 99
        val number1 = Random.nextInt(0, 99)
        val number2 = Random.nextInt(0, 99)

        // Storing the correct answer
        correctAnswer = number1 - number2

        // Create a subtraction problem with these numbers
        val problem = "$number1 - $number2"

        binding.tvEquation.text = problem

        return problem
    }

    //Function which generates a multiplication problem
    private fun generateMultiplicationProblem(): String{
        // Generating two random numbers from 0 and 99
        val number1 = Random.nextInt(0, 20)
        val number2 = Random.nextInt(0, 20)

        correctAnswer = number1 * number2

        // Create a multiplication problem with these numbers
        val problem = "$number1 X $number2"

        binding.tvEquation.text = problem

        return problem
    }

    //Function to manage jackpot and player scores
    private fun manageJackpotAndScores(userAnswer: Int?) {
        if (shouldRollAgain) {

            if (lastDiceRollResult == 5) {
                // Handles losing a turn if the player rolls a 5 after rolling a 4
                shouldRollAgain = false
                doublePointsNextRoll = false
                return

            } else if (doublePointsNextRoll) {
                // Check if the re-roll was due to a prior roll of 4 and handle double points
                if (userAnswer == correctAnswer) {
                    when (lastDiceRollResult) {
                        6 -> {
                            currentPlayer.score += (2 * jackpot)
                            resetJackpot()
                        }
                        3 -> { currentPlayer.score += 6 }
                        2 -> { currentPlayer.score += 4 }
                        else -> currentPlayer.score += 2 // Award double points for correct answer
                    }

                } else {
                    // Handles incorrect answer when double points are expected
                    when (lastDiceRollResult) {

                        // If the user rolls a 6 after rolling a 4 and answers incorrectly
                        6 -> { jackpot += ((2 * jackpot)) }

                        // If the user rolls a 3 after rolling a 4 and answers incorrectly
                        3 -> { jackpot += 6 }

                        // If the user rolls a 2 after rolling a 4 and answers incorrectly
                        2 -> { jackpot += 4 }

                        // Default case for incorrect answers when expecting double points
                        else -> { jackpot += 2 }
                    }
                }

                shouldRollAgain = false
                doublePointsNextRoll = false
            }

        } else {
            // Normal flow for handling dice rolls and answers

            if (userAnswer == correctAnswer) {
                if (lastDiceRollResult == 6) {
                    currentPlayer.score += jackpot  //Player receives jackpot points
                    resetJackpot()
                } else {
                    when (lastDiceRollResult) {
                        3 -> { currentPlayer.score += 3 }
                        2 -> { currentPlayer.score += 2 }
                        else -> currentPlayer.score += 1
                    }
                }
            } else {
                // Increases the jackpot for incorrect answers or attempts at the jackpot question
                if (lastDiceRollResult == 6) {
                    jackpot *= 2 //Jackpot doubles
                } else {
                    jackpot += when (lastDiceRollResult) {
                        3 -> 3  //Jackpot increase by 3
                        2 -> 2  //Jackpot increase by 2
                        else -> 1   //Jackpot increase by 1
                    }
                }
            }
        }
        // Checking if win condition is met
        checkWinConditionAndUpdateUI()
    }

    //Function to display notification to user
    private fun userNotifications(userAnswer: Int?) {
        // Early return if userAnswer is null
        if (userAnswer == null) {
            binding.tvUserNotification.text = "Enter a number."
            return
        }

        // Determining the notification based on game conditions and user answer
        val notificationMessage = when {
            shouldRollAgain && lastDiceRollResult == 5 -> "Correct! Jackpot won."
            doublePointsNextRoll && userAnswer == correctAnswer && lastDiceRollResult == 6 -> "Double Jackpot points gained!!"
            doublePointsNextRoll && userAnswer == correctAnswer -> "Correct! Double points awarded."
            doublePointsNextRoll -> "Incorrect. No points awarded."
            userAnswer == correctAnswer -> "Correct!"
            else -> "Incorrect!"
        }

        // Sets the notification text
        binding.tvUserNotification.text = notificationMessage

        // After displaying the notification, manage the jackpot and scores
        manageJackpotAndScores(userAnswer)
    }


    //Function that switches turns between players
    private fun switchTurns() {
        currentPlayer = if (currentPlayer == player1) player2 else player1

        // Updates UI to show current player's turn
        binding.tvCurrentPlayer.text = "${currentPlayer.name}"
    }

    //Function to check whether or not a user has won the game
    private fun checkWinConditionAndUpdateUI() {
        // Check for win condition after updating the score
        if(currentPlayer.score >= 20){
            showWinDialog()                         // Prompts user if they want to reset the game or not
        }

        updatePlayerScoresUI()                      // Update the UI with the new scores
        binding.tvJackpotTotal.text = "$jackpot"    // Update the jackpot display

        if (!shouldRollAgain) {
            switchTurns()
        }
    }

    //Function used to reset game
    private fun resetGame(){
        player1.score = 0
        player2.score = 0
        currentPlayer = player1
        shouldRollAgain = false
        doublePointsNextRoll = false
        binding.tvPlayer1Total.text = "0"
        binding.tvPlayer2Total.text = "0"
        binding.tvJackpotTotal.text = "5"
        binding.tvUserNotification.text = ""
        binding.tvEquation.text = ""
        binding.etUserAnswer.text = null
    }

    // Function to reset jackpot to initial value 5
    private fun resetJackpot() { jackpot = 5 }

    //Function to update the scores of the players in the UI
    private fun updatePlayerScoresUI() {
        binding.tvPlayer1Total.text = "${player1.score}"
        binding.tvPlayer2Total.text = "${player2.score}"
    }

    //Dialog function to prompt user upon winning whether they want to try again or not
    private fun showWinDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Congratulations, ${currentPlayer.name} wins! Would you like to play again?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id -> resetGame() }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
                finish()        // Close the current activity and exit the app gracefully
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Game Over")
        alert.show()
    }

}

class Player(val name: String) { var score: Int = 0  }


// Class representing a dice with a given number of sides.
class Dice(private val numSides: Int) {
    // Function to roll the dice and return a random number within its range.
    fun diceRoll(): Int {
        return (1..numSides).random() // Return a random number between 1 and numSides.
    }
}