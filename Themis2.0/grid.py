import sys
from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import xml.etree.ElementTree as ET

import themis2
 
class App(QDialog):
 
    def __init__(self):
        super().__init__()
        self.title = 'Themis 2.0'
        self.left = 100
        self.top = 100
        self.width = 800
        self.height = 1000
        self.tree = None
        self.initUI()
 
    def initUI(self):
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)
        
 
        self.createThemisGrid()
        
        windowLayout = QVBoxLayout()
        windowLayout.addWidget(self.horizontalGroupBox4)
        windowLayout.addWidget(self.horizontalGroupBox)
        windowLayout.addWidget(self.horizontalGroupBox2)
        windowLayout.addWidget(self.horizontalGroupBox3)
        self.setLayout(windowLayout)
        
        self.show()

    def createThemisGrid(self):
        
        self.horizontalGroupBox4 = QGroupBox()
        layout4 = QGridLayout()

        command_box_label = QLabel("Command:")
        self.command_box = QLineEdit(self)

        seed_box_label = QLabel("Random seed:")
        self.seed_box = QLineEdit(self)

        max_box_label = QLabel("Max Samples:")
        self.max_box = QLineEdit(self)

        min_box_label = QLabel("Min Samples:")
        self.min_box = QLineEdit(self)

        layout4.addWidget(command_box_label,1, 0)
        layout4.addWidget(self.command_box,1, 1)

        layout4.addWidget(seed_box_label,2,0)
        layout4.addWidget(self.seed_box,2,1)

        layout4.addWidget(max_box_label,3,0)
        layout4.addWidget(self.max_box,3,1)

        layout4.addWidget(min_box_label,4,0)
        layout4.addWidget(self.min_box,4,1)
        
        self.horizontalGroupBox = QGroupBox("Inputs")
        layout = QGridLayout()
        layout.setSpacing(5)

        self.createInputsTable()

        load_button = QPushButton('Load...')
        load_button.clicked.connect(self.handleLoadButton)
        
        save_button = QPushButton('Save...')
        save_button.clicked.connect(self.handleSaveButton)
        
        add_button = QPushButton('Add Input...')
        add_button.clicked.connect(self.handleAddButton)
        self.dialog = EditInputWindow()

        layout.addWidget(self.inputs_table,0,1, 3, 4)
        layout.addWidget(load_button,5, 1)
        layout.addWidget(save_button,5, 2)
        layout.addWidget(add_button,5,3)

        self.horizontalGroupBox2 = QGroupBox("Tests")
        layout2 = QGridLayout()
        self.createTestsTable()

        add_test_button = QPushButton("Add Test...")

        layout2.addWidget(self.tests_table, 5, 4, 4, 4)
        layout2.addWidget(add_test_button, 9, 4)


        self.horizontalGroupBox3 = QGroupBox("")
        layout3 = QGridLayout()

        run_button = QPushButton("Run")
        # run themis
        run_button.clicked.connect(self.runThemis)

        self.results_box = QTextEdit()
        self.results_box.setReadOnly(True)
        
        layout3.addWidget(run_button,1, 1)
        layout3.addWidget(self.results_box, 2, 1, 5, 5)
        
 
        self.horizontalGroupBox.setLayout(layout)
        self.horizontalGroupBox2.setLayout(layout2)
        self.horizontalGroupBox3.setLayout(layout3)
        self.horizontalGroupBox4.setLayout(layout4)

    def runThemis(self):

        self.tester.run()

        self.results_box.setText(self.getTesterOutput)


    def getTesterOutput(self):

        results = self.tester.output

        return results
        
    def handleAddButton(self):
        self.dialog.setModal(True)
        self.dialog.show()

    def handleLoadButton(self):
        dialog = QFileDialog()
        filename = dialog.getOpenFileName(self, "Open File", "/home")

        if filename[0]:
            self.file = open(filename[0], 'r')
            # add themis instance with loaded file        
        
       
        self.processSettingsFiles()

    def processSettingsFiles(self):        
        self.tree = ET.parse(self.file)
        root = self.tree.getroot()


        run_command = root.find('command').text
        self.command_box.setText(run_command)

        seed = root.find('seed').text
        self.seed_box.setText(seed)

        max_samples = root.find('max_samples').text
        self.max_box.setText(max_samples)


        min_samples = root.find('min_samples').text
        self.min_box.setText(min_samples)

        # column 1 = Input Name
        # column 2 = Input Type
        # column 3 = Values
        
        #for categorical values
        self.inputs = []
        ctr = 0
        
        for run_input in root.iter('input'):
            name = run_input.find('name').text
            print(name)
            self.inputs[ctr] = name
            
            categoricalFlag = False
            for j in run_input.iter('type'):

                if j.text == "categorical":

                    categoricalFlag = True
            values = []
            if(categoricalFlag is True):
                for i in run_input.iter('value'):

                    values.append(i.text)
            else:

                for lbound in run_input.iter('lowerbound'):

                    values.append(lbound.text)
                for ubound in run_input.iter('upperbound'):
                    values.append(ubound.text)


            if (len(values) != 0):
                self.setCellValue(values.__str__(), ctr, 3)
            ctr += 1

            index = 0
            
            for run_test in root.iter('test'):
                function = ""
                configuration = ""
                margin = ""
                for func in run_test.iter("function"):
                    function = func.text
                for config in run_test.iter("conf"):
                    configuration = config.text
                for marg in run_test.iter("margin"):
                    margin = marg.text
                    
                print(function)
                print(configuration)
                print(margin)
                print("Got all the values")
                
                self.setTestTableValue(function,index,1)                
                self.setTestTableValue(configuration, index, 2)
                self.setTestTableValue(margin, index, 3)
                index += 1


    def handleSaveButton(self):
        self.tree.write("settings")

    def createInputsTable(self):        
        
        self.inputs_table = QTableWidget()
        self.inputs_table.setRowCount(10)
        self.inputs_table.setColumnCount(4)
        self.inputs_table.setHorizontalHeaderLabels(["", "Input Name", "Input Type", "Values"])

        # pass in row to create buttons on that row
        for i in range(self.inputs_table.rowCount()):
            self.createEditButtons(self.inputs_table, i)
        
        self.inputs_table.horizontalHeader().setStretchLastSection(True)
        self.inputs_table.resizeRowsToContents()
        self.inputs_table.resizeColumnsToContents()
        self.inputs_table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.inputs_table.verticalHeader().setVisible(False)

    def createTestsTable(self):
        self.tests_table = QTableWidget()
        self.tests_table.setRowCount(10)
        self.tests_table.setColumnCount(5)
        self.tests_table.setHorizontalHeaderLabels(["", "Name", "Confidence", "Margin", "Notes"])

        for i in range(self.tests_table.rowCount()):
            self.createEditButtons(self.tests_table,i)
        
        self.tests_table.horizontalHeader().setStretchLastSection(True)
        self.tests_table.resizeRowsToContents()
        self.tests_table.resizeColumnsToContents()
        self.tests_table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.tests_table.verticalHeader().setVisible(False)

    def createEditButtons(self, table, row):
        layout = QHBoxLayout()
        layout.setContentsMargins(2,2,2,2)
        layout.setSpacing(10)
        
        delete_btn = QPushButton(table)
        delete_btn.setText("Delete")
        delete_btn.adjustSize()
        layout.addWidget(delete_btn)

        edit_btn = QPushButton(table)
        edit_btn.setText("Edit...")
        layout.addWidget(edit_btn)
        cellWidget = QWidget()
        cellWidget.setLayout(layout)
        
        table.setCellWidget(row,0,cellWidget)

    

    def setCellValue(self, value, row, column):
        new_input = QTableWidgetItem()
        new_input.setText(value)
        self.inputs_table.setItem(row,column,new_input)

    def setTestTableValue(self, value, row, column):
        new_input = QTableWidgetItem()
        new_input.setText(value)
        self.tests_table.setItem(row,column,new_input)

    
class EditInputWindow(QDialog):

    def __init__(self):
        super().__init__()
        self.title = 'Add or Edit Inputs'
        self.left = 100
        self.top = 100
        self.width = 500
        self.height = 300
        self.initUI()

    def initUI(self):
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)

        self.createGrid()

        windowLayout = QVBoxLayout()
        windowLayout.addWidget(self.horizontalGroupBox)
        self.setLayout(windowLayout)

##        self.show()

    def createGrid(self):
        self.horizontalGroupBox = QGroupBox("")
        layout = QGridLayout()

        name_label = QLabel("Input name: ")
        self.name_box = QLineEdit(self)

        layout.addWidget(name_label, 1, 1)
        layout.addWidget(self.name_box, 1, 2)

        type_label = QLabel("Input type: ")
        self.types = QComboBox()
        self.types.addItem("Categorical")
        self.types.addItem("Continuous Int")
        

        layout.addWidget(type_label, 2, 1)
        layout.addWidget(self.types, 2, 2)
        self.values_label = QLabel("Values (separated by commas): ")
        self.values_box = QLineEdit(self)
            
        layout.addWidget(self.values_label, 3, 1)
        layout.addWidget(self.values_box, 3, 2)
        
        self.types.currentIndexChanged.connect(self.selectionChange)

        self.add_button = QPushButton("Add")

        layout.addWidget(self.add_button, 4, 1)

        self.done_button = QPushButton("Done")

        layout.addWidget(self.done_button, 4, 4)
        
        self.horizontalGroupBox.setLayout(layout)

    def selectionChange(self):

        if self.types.currentText() == "Continuous Int":
            self.values_label.setText("Enter range (e.g. 1-10) : ")
        else:
            self.values_label.setText("Values (separated by commas): ")
            




if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = App()
    sys.exit(app.exec_())
