package com.myapp.app101.game;


import java.util.ArrayList;
import java.util.Arrays;

public class SudokuSolver {

    private int NUM_ROWS = 9;
    private int NUM_COLS = 9;
    private int NUM_VALS = 9;
    private int NUM_CONSTRAINTS = 4;
    private int NUM_CELLS = NUM_ROWS * NUM_COLS;

    private int[][] mConstraintMatrix;
    private Node[][] mLinkedList;
    private Node mHead;
    private ArrayList<Node> mSolution;

    public SudokuSolver() {
        initializeConstraintMatrix();
        initializeLinkedList();
    }


    public void setPuzzle(CellCollection mCells) {
        Cell[][] board = mCells.getCells();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Cell cell = board[row][col];
                int val = cell.getValue();
                if (!cell.isEditable()) {
                    int matrixRow = cellToRow(row, col, val - 1, true);
                    int matrixCol = 9 * row + col; // calculates column of node based on cell constraint

                    Node rowNode = mLinkedList[matrixRow][matrixCol];
                    Node rightNode = rowNode;
                    do {
                        cover(rightNode);
                        rightNode = rightNode.right;
                    } while (rightNode != rowNode);
                }
            }
        }
    }

    public ArrayList<int[]> solve() {
        mSolution = new ArrayList<>();
        mSolution = DLX();

        ArrayList<int[]> finalValues = new ArrayList<>();
        for (Node node : mSolution) {
            int matrixRow = node.rowID;
            int[] rowColVal = rowToCell(matrixRow, true);
            finalValues.add(rowColVal);
        }

        return finalValues;
    }


    private void initializeConstraintMatrix() {
        mConstraintMatrix = new int[NUM_ROWS * NUM_COLS * NUM_VALS + 1][NUM_CELLS * NUM_CONSTRAINTS];
        Arrays.fill(mConstraintMatrix[0], 1);


        int rowShift = NUM_CELLS;
        int colShift = NUM_CELLS * 2;
        int blockShift = NUM_CELLS * 3;
        int cellColumn = 0;
        int rowColumn = 0;
        int colColumn = 0;
        int blockColumn = 0;

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                for (int val = 0; val < NUM_VALS; val++) {
                    int matrixRow = cellToRow(row, col, val, true);

                    mConstraintMatrix[matrixRow][cellColumn] = 1;

                    mConstraintMatrix[matrixRow][rowColumn + rowShift] = 1;
                    rowColumn++;

                    mConstraintMatrix[matrixRow][colColumn + colShift] = 1;
                    colColumn++;

                    mConstraintMatrix[matrixRow][blockColumn + blockShift] = 1;
                    blockColumn++;
                }
                cellColumn++;
                rowColumn -= 9;
                if (col % 3 != 2) {
                    blockColumn -= 9;
                }
            }
            rowColumn += 9;
            colColumn -= 81;
            if (row % 3 != 2) {
                blockColumn -= 27;
            }
        }
    }

    private void initializeLinkedList() {
        mLinkedList = new Node[NUM_ROWS * NUM_COLS * NUM_VALS + 1][NUM_CELLS * NUM_CONSTRAINTS];
        mHead = new Node();
        int rows = mLinkedList.length;
        int cols = mLinkedList[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mConstraintMatrix[i][j] == 1) {
                    mLinkedList[i][j] = new Node();
                }
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mConstraintMatrix[i][j] == 1) {
                    int a, b;

                    a = i;
                    b = j;
                    do {
                        b = moveLeft(b, cols);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].left = mLinkedList[a][b];

                    a = i;
                    b = j;
                    do {
                        b = moveRight(b, cols);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].right = mLinkedList[a][b];

                    a = i;
                    b = j;
                    do {
                        a = moveUp(a, rows);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].up = mLinkedList[a][b];

                    a = i;
                    b = j;
                    do {
                        a = moveDown(a, rows);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].down = mLinkedList[a][b];

                    mLinkedList[i][j].columnHeader = mLinkedList[0][j];
                    mLinkedList[i][j].rowID = i;
                    mLinkedList[i][j].colID = j;
                }
            }
        }

        mHead.right = mLinkedList[0][0];
        mHead.left = mLinkedList[0][cols - 1];
        mLinkedList[0][0].left = mHead;
        mLinkedList[0][cols - 1].right = mHead;
    }


    public ArrayList<Node> DLX() {
        if (mHead.right == mHead) {
            return mSolution;
        } else {
            Node colNode = chooseColumn();
            cover(colNode);

            Node rowNode;
            for (rowNode = colNode.down; rowNode != colNode; rowNode = rowNode.down) {
                mSolution.add(rowNode);

                Node rightNode;
                for (rightNode = rowNode.right; rightNode != rowNode; rightNode = rightNode.right) {
                    cover(rightNode);
                }

                ArrayList<Node> tempSolution = DLX();
                if (!tempSolution.isEmpty()) {
                    return tempSolution;
                }

                mSolution.remove(mSolution.size() - 1);
                colNode = rowNode.columnHeader;
                Node leftNode;
                for (leftNode = rowNode.left; leftNode != rowNode; leftNode = leftNode.left) {
                    uncover(leftNode);
                }
            }
            uncover(colNode);
        }
        return new ArrayList<>();
    }



    private int cellToRow(int row, int col, int val, boolean headersInMatrix) {
        int matrixRow = 81 * row + 9 * col + val;
        if (headersInMatrix) {
            matrixRow++;
        }
        return matrixRow;
    }

    private int[] rowToCell(int matrixRow, boolean headersInMatrix) {
        int[] rowColVal = new int[3];
        if (headersInMatrix) {
            matrixRow--;
        }

        rowColVal[0] = matrixRow / 81;
        rowColVal[1] = matrixRow % 81 / 9;
        rowColVal[2] = matrixRow % 9 + 1;
        return rowColVal;
    }

    private int moveLeft(int j, int numCols) {
        return j - 1 < 0 ? numCols - 1 : j - 1;
    }

    private int moveRight(int j, int numCols) {
        return (j + 1) % numCols;
    }

    private int moveUp(int i, int numRows) {
        return i - 1 < 0 ? numRows - 1 : i - 1;
    }

    private int moveDown(int i, int numRows) {
        return (i + 1) % numRows;
    }


    private void cover(Node node) {
        Node colNode = node.columnHeader;
        colNode.left.right = colNode.right;
        colNode.right.left = colNode.left;

        Node rowNode;
        for (rowNode = colNode.down; rowNode != colNode; rowNode = rowNode.down) {
            Node rightNode;
            for (rightNode = rowNode.right; rightNode != rowNode; rightNode = rightNode.right) {
                rightNode.up.down = rightNode.down;
                rightNode.down.up = rightNode.up;
                rightNode.columnHeader.count--;
            }
        }
    }

    private void uncover(Node node) {
        Node colNode = node.columnHeader;
        Node upNode;
        for (upNode = colNode.up; upNode != colNode; upNode = upNode.up) {
            Node leftNode;
            for (leftNode = upNode.left; leftNode != upNode; leftNode = leftNode.left) {
                leftNode.up.down = leftNode;
                leftNode.down.up = leftNode;

                leftNode.columnHeader.count++;
            }
        }
        colNode.left.right = colNode;
        colNode.right.left = colNode;
    }


    private Node chooseColumn() {
        Node bestNode = null;
        int lowestNum = 100000;

        Node currentNode = mHead.right;
        while (currentNode != mHead) {
            if (currentNode.count < lowestNum) {
                bestNode = currentNode;
                lowestNum = currentNode.count;
            }
            currentNode = currentNode.right;
        }
        return bestNode;
    }
}
