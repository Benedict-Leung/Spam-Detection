# CSCI2020 Assignment 1

An application to detect spam using Naive Bayes spam filtering

![Preview](./SpamDetection.png?raw=true)

# Interface improvements
- Changed the font to `Montserrat`
- Color scheme to match Ontario Tech University colors
- A visual representation of the accuracy and precision
- Shows distribution of ham(H) files and spam(S) files in training and testing phase

# Model features

### Naive Bayes spam filtering
#### First computation: 
  
  ![Equation](https://latex.codecogs.com/svg.latex?Pr(W_i|S)=\frac{Pr(W_i|S)}{Pr(S|W_i)&space;&plus;&space;Pr(W_i|H)})

#### Second Computation:

  ![Equation](https://latex.codecogs.com/svg.latex?\eta&space;=&space;\sum_{i&space;=&space;1}^{N}[\ln&space;(1-Pr(S|W_i))-ln(Pr(S|W_i))])

  ![Equation](https://latex.codecogs.com/svg.latex?Pr(S|F)&space;=&space;\frac{1}{1&plus;e^\eta})

#### Third Computation (Improved detection):
  
  ![Equation](https://latex.codecogs.com/svg.latex?Pr'(S|W_i)=&space;\frac{s&space;\cdot&space;Pr(S)&space;&plus;&space;n&space;\cdot&space;Pr(S|W_i)}{s&plus;n})

  Where $Pr(S) = 0.5$, $s = 4$ and $n = $ number of files that have $W_i$
#### Stop words (Improved detection):
Does not evaluate stop words specified in `stopwords.txt`,

# Build Instructions for IntelliJ
### Prerequisites
- JDK 15.X.X+
- JavaFX 15.X.X+

Make sure to add the JavaFX library (`File -> Project Structure -> Libraries`) and add VM options (`Run -> Edit Configurations`) to IntelliJ

(VM options: `--module-path /path/to/javafx/sdk --add-modules javafx.controls,javafx.fxml`)

# Reference
- https://www.jetbrains.com/help/idea/javafx.html#create-project - Reference to running JavaFX projects on IntelliJ
- https://towardsdatascience.com/email-spam-detection-1-2-b0e06a5c0472 - Reference to data distributions and stop words
- https://gist.github.com/sebleier/554280 - Reference to stop words list
- https://en.wikipedia.org/wiki/Naive_Bayes_spam_filtering#cite_note-14 - Reference to third computation for rare words