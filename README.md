# Sudoku_Solver
Sudoku Solver for AI project


Implementing the following heuristics for Sudoku Solver

MRV -> Minimum Remaining Values in the domain : Return a variable
LCV -> Least Constraining Value : Return a variable
MAD -> MRV with Degree Heuristic for tie break : Return a list of variables
FC -> Forward checking. Return list of updated variables as a map
NOR -> Norvig Heuristic. Return list of newly assigned variables.


To execute the solver : java -jar bin/Sudoku.jar <list of heuritics to be applied > <path to sudoku to be solved>
  
  
To execute the python generator : 
python board_generator.py <prefix of the test_files> <# num of files > < rows > < cols > < num of prepopulated cells >
