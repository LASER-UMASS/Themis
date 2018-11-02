import sys
from PyQt5.QtWidgets import *
import PyQt5.QtGui as QtGui
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import xml.etree.ElementTree as ET

import themis2

tree = None

class App(QDialog):
 
    def __init__(self):
        super().__init__()
        self.title = 'Themis 2.0'
        self.left = 500
        self.top = 200
        self.width = 600
        self.height = 700
        self.dialog = None
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
        self.tester = themis2.Themis()

        command_box_label = QLabel("Command:")
        self.command_box = QLineEdit(self)

        layout4.addWidget(command_box_label,1, 0)
        layout4.addWidget(self.command_box,1, 1)


        self.createInputsTable()
        self.createTestsTable()

        self.horizontalGroupBox = QGroupBox("Inputs")
        self.horizontalGroupBox.setFont(QFont("", pointSize=14))
        layout = QGridLayout()
        layout.setSpacing(5)
        
        add_button = QPushButton('Add Input')
        add_button.setAutoDefault(False)

        add_button.clicked.connect(self.addInput)

        layout.addWidget(self.inputs_table,0,1, 3, 4)
        layout.addWidget(add_button,5,1)

        self.horizontalGroupBox2 = QGroupBox("Measurements")
        self.horizontalGroupBox2.setFont(QFont("", pointSize=14))
        layout2 = QGridLayout()
        
        add_test_button = QPushButton('Add Measurement')
        add_test_button.setAutoDefault(False)

        add_test_button.clicked.connect(self.addTest)

        layout2.addWidget(self.tests_table, 5, 4, 4, 4)
        layout2.addWidget(add_test_button, 9, 4)


        self.horizontalGroupBox3 = QGroupBox("")
        self.layout3 = QGridLayout()

        self.results_box = QTextEdit()
        self.results_box.setReadOnly(True)

        run_button = QPushButton("Run")
        run_button.clicked.connect(self.handleRunButton)
        run_button.setAutoDefault(False)

        load_button = QPushButton('Load')
        load_button.setAutoDefault(False)
        
        save_button = QPushButton('Save')
        save_button.setAutoDefault(False)


        save_button.clicked.connect(self.handleSaveButton)
        load_button.clicked.connect(self.handleLoadButton)

        self.layout3.addWidget(load_button,1, 1)
        self.layout3.addWidget(save_button,1, 2)
        
        self.layout3.addWidget(run_button,1, 3)
        self.layout3.addWidget(self.results_box, 2, 1, 5, 5)        

        self.horizontalGroupBox3.setLayout(self.layout3)
        self.horizontalGroupBox.setLayout(layout)
        self.horizontalGroupBox2.setLayout(layout2)
        
        self.horizontalGroupBox4.setLayout(layout4)

    def addTest(self):
        inputs = []
        for i in range(self.inputs_table.rowCount()):
            print(self.inputs_table.item(i,1).text())
            inputs.append(self.inputs_table.item(i,1))
            
        
        self.dialog = EditTestWindow(True)
        self.dialog.setModal(True)
        self.dialog.show()
        
        if self.dialog.exec_():
            tests = self.dialog.all_measurements
            conf_values = self.dialog.all_confs
            margins = self.dialog.all_margins
            inputs = self.dialog.all_inputs
            #add thresholds
                
            
            for i in range(len(tests)):
                print ("Measurement: " + tests[i])
                print ("Confidence: " + conf_values[i])
                print ("Margin: " + margins[i])
                print ("Inputs of interest: " + ",".join(inputs[i]))

                test = tests[i]
                print (test)
                conf = conf_values[i]
                margin = margins[i]
                i_fields = list(inputs[i])

                self.tests_table.insertRow(i)
                self.createEditButtons(self.tests_table, i, test=None)

                self.setTestTableValue(conf, i, 2)
                self.setTestTableValue(margin, i, 3)
                self.setTestTableValue(",".join(i_fields), i, 4)

                if test == "Group Discrimination":
                    test = self.tester._new_test(False, False, "group_discrimination", float(conf), float(margin), i_fields)
                    self.setTestTableValue(test, i, 1)
                elif test == "Causal Discrimination":
                    test = self.tester._new_test(False, False, "causal_discrimination", float(conf), float(margin), i_fields)
                    self.setTestTableValue(test, i, 1)

                else:
                    
                    
                    if test == "Discrimination Search Causal":
                        self.setTestTableValue("Search for causal discrimination", i, 1)
                        test = self.tester._new_test( False, True, "discrimination_search", float(conf), float(margin),i_fields)
                    elif test == "Discrimination Search Group":
                        print("HERE 1")
                        self.setTestTableValue("Search for group discrimination", i, 1)
                        test = self.tester._new_test( True, False, "discrimination_search", float(conf), float(margin), i_fields)
                    else:
                        print ("HERE 2")
                        self.setTestTableValue("Search for group & causal discrimination", i, 1)
                        test = self.tester._new_test(True, True, "discrimination_search", float(conf), float(margin), i_fields)
                
        self.resizeCells(self.tests_table)
                

    def addInput(self):
        dialog = EditInputWindow(True)
        dialog.setModal(True)
        dialog.show()

        if dialog.exec_():
           for i in range(len(dialog.all_names)):
                print("Name: " + dialog.all_names[i])
                print("Type: " + dialog.all_types[i])
                print("Values: " + dialog.all_values[i])

                name = dialog.all_names[i]
                kind = dialog.all_types[i]
                values = dialog.all_values[i]

                self.inputs_table.insertRow(i)
                self.createEditButtons(self.inputs_table, i, test=None)

                self.setCellValue(name, i, 1)
                self.setCellValue(kind, i, 2)
                
                if kind == "Categorical":
                    self.setCellValue("{" + values + "}", i, 3)
                    
                    self.tester._add_input(name, "Categorical", values)
                else:
                    ulb = values.split('-')
                    lb = ulb[0]
                    ub = ulb[1]
                    self.setCellValue("(" + lb + "-" + ub + ")", i, 3)
                    
                    self.tester._add_input(name, "Continuous Int", values)
                    
        self.resizeCells(self.inputs_table)               

        
    def handleRunButton(self):
        #python loan.py
        #42
        #200
        #10
        self.tester.max_samples = 10000
        self.tester.min_samples = 10
        self.tester.rand_seed = 42
        self.tester.command = self.command_box.text()

        self.tester.run()

        self.results_box.setText("<h2 style=\"text-align:center\">Themis 2.0 Execution Complete!</h2>");
        self.results_box.repaint()

        for test in self.tester.tests:
            print (test)
            if test.group == True or test.causal == True:
                if self.tester.group_search_results or self.tester.causal_search_results:
                    self.results_box.append("<h2> Discrimination found! </h2>")
                    self.results_box.repaint()
                    detailed_output_btn = QPushButton("More details...")
                    self.layout3.addWidget(detailed_output_btn, 8, 4)
                    detailed_output_btn.clicked.connect(self.handleDetailedButton)
                    self.horizontalGroupBox3.setLayout(self.layout3)

                    
        self.results_box.toHtml()


    def handleDetailedButton(self):
        dialog = QDialog()

        horizontalGroupBox = QGroupBox("Detailed Discrimination Findings")
        horizontalGroupBox.setFont(QFont("", pointSize=14))
        layout = QGridLayout()
        
        detailed_output_box = QTextEdit()
        detailed_output_box.setReadOnly(True)
        detailed_output_box.setText("")

##        if self.tester.group_tests:
##            detailed_output_box.append("<h2>Group Discrimination Tests</h2>")
##        if self.tester.causal_tests:
##            detailed_output_box.append("<h2>Causal Discrimination Tests</h2>")
        for test in self.tester.tests:
            if test.group == True or test.causal == True:
                detailed_output_box.append("<h2>Discrimination Results</h2>")
                detailed_output_box.append("<h4> Threshold: " + "{:.1%}".format(test.threshold) + "</h4>")

                if self.tester.group_search_results:
                    detailed_output_box.append("<h3> Group discrimination found </h3>")
                    for key,value in self.tester.group_search_results.items():
                        detailed_output_box.append("<h3>" + ",".join(key) + ": " + value + "</h3>")
                        print (",".join(key) + "-->" + value )
                        for result in self.tester.group_measurement_results:
                            if (key == tuple(result.i_fields)):
                                min_score = result.data[0]
                                min_assign = result.data[1]

                                max_score = result.data[2]
                                max_assign = result.data[3]

                                detailed_output_box.append("&nbsp; &nbsp; &nbsp; <b>Min: </b>" + min_assign + " &#8594; " + "{:.1%}".format(min_score))
                                detailed_output_box.append("&nbsp; &nbsp; &nbsp; <b>Max: </b>" + max_assign + " &#8594; " + "{:.1%}".format(max_score))

                if self.tester.causal_search_results:
                    detailed_output_box.append("<h3>Causal discrimination found</h3>")
                    for key,value in self.tester.causal_search_results.items():
                        detailed_output_box.append("<h3>" + ",".join(key) + ": " + value + "</h3>")
                        print (",".join(key) + "-->" + value)
                        
                        detailed_output_box.append("<h4> &nbsp; &nbsp; &nbsp; Causal Pairs: </h4>")
                        for result in self.tester.causal_measurement_results:
                            if (key == tuple(result.i_fields)):
                                for a1, a2 in result.data:

                                    str_list1 = []
                                    str_list2 = []
                                    for k in self.tester.input_order:
                                        if k in key:
                                            str_list1.append("<b>" + a1[k] + "</b>")
                                            str_list2.append("<b>" + a2[k] + "</b>")
                                        else:
                                            str_list1.append(a1[k])
                                            str_list2.append(a2[k])

                                    causal_pair1 = ", ".join(str_list1)
                                    causal_pair2 = ", ".join(str_list2)
                                        

                                    detailed_output_box.append("<p>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; " + causal_pair1 + " &#8594; " + causal_pair2 + "</p>")

                                    print (causal_pair1, causal_pair2)
                                
                                
        detailed_output_box.toHtml()

        detailed_output_box.moveCursor(QtGui.QTextCursor.Start)

        layout.addWidget(detailed_output_box, 1, 1, 5, 5)
        horizontalGroupBox.setLayout(layout)

        dialog.setGeometry(self.left, self.top, self.width+100, self.height-200)

        windowLayout = QVBoxLayout()
        windowLayout.addWidget(horizontalGroupBox)
        dialog.setLayout(windowLayout)

        dialog.setWindowTitle("Themis 2.0: Detailed Output")
        dialog.exec_()

            

    def handleLoadButton(self):
        dialog = QFileDialog()
        filename = dialog.getOpenFileName(self, "Open File", "~/Documents/Themis/Themis2.0")

        if filename[0]:
            self.file = open(filename[0], 'r')
        else:
            return

        self.tester = themis2.Themis(filename[0])

        command =  self.tester.command
        rand_seed = self.tester.rand_seed
        max_samples = self.tester.max_samples
        min_samples = self.tester.min_samples
        
        # set text boxes from Themis

        self.command_box.setText(command)
        self.results_box.clear()

        self.tests_table.clearContents()
        self.inputs_table.clearContents()


        index = 0
        inputs = []
        for test in self.tester.tests:
            self.tests_table.insertRow(index)
            self.createEditButtons(self.tests_table,index, test)
            
            function = test.function
            confidence = test.conf
            margin = test.margin

            self.setTestTableValue(str(function),index,1)
            self.setTestTableValue(str(confidence), index, 2)
            self.setTestTableValue(str(margin), index, 3)

            index += 1

            for field in test.i_fields:
                if field not in inputs:
                    inputs.append(field)
            

            self.resizeCells(self.tests_table) 
        
        i = 0
        for name in self.tester.input_names:
            self.inputs_table.insertRow(i)
            self.createEditButtons(self.inputs_table, i, test)
            
            inpt = self.tester.inputs[name]
            
            name = inpt.name
            inpt_type = inpt.kind
            values = inpt.values

            self.setCellValue(name, i, 1)
            self.setCellValue(inpt_type, i, 2)

            if inpt_type == "categorical":
                value = "{" + ", ".join(values) + "}"
                self.setCellValue(value, i, 3)
            else:
                value = "[" + inpt.lb + "-" + inpt.ub + "]"
                self.setCellValue(value, i, 3)
                    
            i +=1

        self.resizeCells(self.inputs_table)

    def resizeCells(self, table):
        
        table.resizeRowsToContents()
 
        table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        table.verticalHeader().setVisible(False)
        table.horizontalHeader().setStretchLastSection(True)

        for i in range(table.columnCount()-1):
            table.resizeColumnToContents(i)      

            
            
    def handleSaveButton(self):

        return
        
    def createInputsTable(self):        
        
        self.inputs_table = QTableWidget()
        self.inputs_table.setColumnCount(4)
        self.inputs_table.setHorizontalHeaderLabels(["", "Input Name", "Input Type", "Possible Values"])
        self.inputs_table.horizontalHeader().setStretchLastSection(True)

        self.resizeCells(self.inputs_table)

    def createTestsTable(self):
        self.tests_table = QTableWidget()
        self.tests_table.setColumnCount(5)
        self.tests_table.setHorizontalHeaderLabels(["", "Measurement To Run", "Confidence", "Margin", "Inputs"])
       
        self.resizeCells(self.tests_table)
        
    def createEditButtons(self, table, row, test):
        layout = QHBoxLayout()
        layout.setContentsMargins(2,2,2,2)
        layout.setSpacing(10)
        
        delete_btn = QPushButton(table)
        delete_btn.setText("Delete")
        delete_btn.adjustSize()
        layout.addWidget(delete_btn)

        edit_btn = QPushButton(table)
        edit_btn.setText("Edit...")
        
        index = QPersistentModelIndex(table.model().index(row, 0))
        
        edit_btn.clicked.connect(lambda *args, index=index: self.handleEditButton(index, table, test))
        
        layout.addWidget(edit_btn)
        
        cellWidget = QWidget()
        cellWidget.setLayout(layout)
        
        table.setCellWidget(row,0,cellWidget)
        
    def handleEditButton(self, index, table, test):

        if table is self.inputs_table:
            item_name = table.item(index.row(), 1)
            item_type = table.item(index.row(), 2)
            item_values = table.item(index.row(), 3)
            print ("Name: " + item_name.text())
            print ("Type: " + item_type.text())
            print ("Values :" + item_values.text())
            
            edit_input_dialog = EditInputWindow(False, item_name.text(), item_type.text(), item_values.text())
            edit_input_dialog.setModal(True)
            edit_input_dialog.show()

        if table is self.tests_table:
            test_name = table.item(index.row(), 1)
            confidence = table.item(index.row(), 2)
            margin = table.item(index.row(), 3)
            
            self.edit_test_dialog = EditTestWindow(False, test_name.text(), confidence.text(), margin.text(), test)
            self.edit_test_dialog.setModal(True)
            self.edit_test_dialog.show()

        self.updateTable(table, index)
            
    def updateTable(self, table, index):
        if self.edit_test_dialog.exec_():
            if table is self.tests_table:
                test = self.tester.tests[index.row()]
                print (" We are in update table")
                print ("Sex ", self.sex_cb.isChecked())
                print ("Race ", self.race_cb.isChecked())
                print ("Income ", self.income_cb.isChecked())
                
                #update Themis values
                if self.edit_test_dialog.sex_cb.isChecked() and self.edit_test_dialog.race_cb.isChecked() and self.edit_test_dialog.income_cb.isChecked():
                    print ("Blah blah blah")
                    # fix wht goes in table!
                    table.item(index.row(),1).setText("discrimination_search")
                    test.function = "discrimination_search"
                    if self.edit_test_dialog.causal_cb.isChecked() and not self.edit_test_dialog.group_cb.isChecked():
                        test.group = False
                        test.causal = True
                    elif not self.edit_test_dialog.causal_cb.isChecked() and self.edit_test_dialog.group_cb.isChecked():
                        test.group = True
                        test.causal = False
                    else:
                        test.group = True
                        test.causal = True
                        
                else:
                    if self.edit_test_dialog.group_cb.isChecked():
                        test.function = "group_discrimination"
                        table.item(index.row(),1,).setText("group_discrimination")
                    elif self.edit_test_dialog.causal_cb.isChecked():
                        table.item(index.row(),1).setText("causal_discrimination")
                        test.function = "causal_discrimination"

                test.conf = float(self.edit_test_dialog.conf_box.text())
                table.item(index.row(), 2).setText(str(test.conf))
                
                test.margin = float(self.edit_test_dialog.margin_box.text())
                table.item(index.row(), 3).setText(str(test.margin))

                self.tester.tests[index.row()] = test


    def setCellValue(self, value, row, column):
        new_input = QTableWidgetItem()
        new_input.setText(value)
        self.inputs_table.setItem(row,column,new_input)

    def setTestTableValue(self, value, row, column):
        new_input = QTableWidgetItem()
        new_input.setText(value)
        self.tests_table.setItem(row,column,new_input)
        

class EditTestWindow(QDialog):
    def __init__(self, inputs=None, add=False, name=None, conf=None, margin=None, test=None):
        super().__init__()
        if add == False:
            self.title = 'Edit Measurement'
        else:
            self.title = 'Add Measurements'
        self.left = 100
        self.top = 100
        self.width = 500
        self.height = 300

        assert inputs != None
        try:
            if name == None and conf == None and margin == None and test==None:
                self.initUI(inputs)
            else:
                self.initUI(inputs, name, conf, margin, test)
        except:
            print('Inputs required to add test!')

    def initUI(self, name=None, conf=None, margin=None, test=None):
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)

        if name == None and conf == None and margin == None and test==None:
            self.createGrid()
        else:
            self.createGrid(name, conf, margin, test)
        
        windowLayout = QVBoxLayout()
        windowLayout.addWidget(self.horizontalGroupBox)
        self.setLayout(windowLayout)


    def createGrid(self, name=None, conf=None, margin=None, test=None):


        self.all_measurements = []
        self.all_confs = []
        self.all_margins = []
        self.all_inputs = []
        self.all_thresholds = []

        self.horizontalGroupBox = QGroupBox("")
        self.layout = QGridLayout()

        self.type_label = QLabel("Measurement Type: ")
        self.group_cb = QCheckBox('Group Discrimination',self)
        self.causal_cb = QCheckBox('Causal Discrimination',self)

        if name is not None:
            if name == "group_discrimination":
                self.group_cb.setChecked(True)
            elif name == "causal_discrimination":
                self.causal_cb.setChecked(True)
            elif name == "discrimination_search":
                if test is not None:
                    self.discrim_search.setChecked(True)
                    if test.group == True:
                        self.group_cb.setChecked(True)
                    if test.causal == True:
                        self.causal_cb.setChecked(True)
                else:
                    print ("No test was passed in!")
                

        self.group_cb.stateChanged.connect(self.selectionChange)
        self.causal_cb.stateChanged.connect(self.selectionChange)
        self.layout.addWidget(self.type_label, 1, 1)
        self.layout.addWidget(self.group_cb, 1, 2)
        self.layout.addWidget(self.causal_cb, 2, 2)

        self.inputs_label = QLabel("Select inputs of interest:")
        self.sex_cb = QCheckBox("sex")
        self.race_cb = QCheckBox("race")
        self.income_cb = QCheckBox("income")

        self.sex_cb.stateChanged.connect(self.selectionChange)
        self.race_cb.stateChanged.connect(self.selectionChange)
        self.income_cb.stateChanged.connect(self.selectionChange)

        self.layout.addWidget(self.inputs_label, 5, 1)
        self.layout.addWidget(self.sex_cb,5,2)
        self.layout.addWidget(self.race_cb,6,2)
        self.layout.addWidget(self.income_cb,7,2)
                    

        self.conf_label = QLabel("Confidence: ")
        self.conf_box = QLineEdit(self)

        if conf is not None:
            self.conf_box.setText(str(conf))

        self.layout.addWidget(self.conf_label, 8, 1)
        self.layout.addWidget(self.conf_box, 8, 2)

        self.margin_label = QLabel("Margin: ")
        self.margin_box = QLineEdit(self)

        if margin is not None:
            self.margin_box.setText(str(margin))

        self.layout.addWidget(self.margin_label, 9, 1)
        self.layout.addWidget(self.margin_box, 9, 2)

        self.add_button = QPushButton("Add")
        self.add_button.setAutoDefault(False)

        self.layout.addWidget(self.add_button, 10, 3)
        self.add_button.clicked.connect(self.handleAddButton)      

        self.horizontalGroupBox.setLayout(self.layout)
        

    def selectionChange(self):

        if self.group_cb.isChecked or self.causal_cb.isChecked():
            if self.sex_cb.isChecked() and self.race_cb.isChecked() and self.income_cb.isChecked():
                self.enterThreshold()
            else:
                try:
                    self.removeThreshold()
                except:
                    return
            
    def removeThreshold(self):
        self.layout.removeWidget(self.threshold_label)
        self.layout.removeWidget(self.threshold_box)
        self.layout.removeWidget(self.conf_label)
        self.layout.removeWidget(self.conf_box)
        self.layout.removeWidget(self.margin_label)
        self.layout.removeWidget(self.margin_box)
        self.layout.removeWidget(self.add_button)

        self.layout.addWidget(self.conf_label, 8, 1 )
        self.layout.addWidget(self.conf_box, 8, 2 )

        self.layout.addWidget(self.margin_label, 9, 1 )
        self.layout.addWidget(self.margin_box, 9, 2 )

        self.layout.addWidget(self.add_button, 10, 3)
        
    def enterThreshold(self):
        self.threshold_label = QLabel("Threshold:")
        self.threshold_box = QLineEdit(self)

        self.layout.removeWidget(self.conf_label)
        self.layout.removeWidget(self.conf_box)
        self.layout.removeWidget(self.margin_label)
        self.layout.removeWidget(self.margin_box)
        self.layout.removeWidget(self.add_button)

        self.layout.addWidget(self.threshold_label, 8, 1)
        self.layout.addWidget(self.threshold_box, 8, 2)
        
        self.layout.addWidget(self.conf_label, 9, 1 )
        self.layout.addWidget(self.conf_box, 9, 2 )

        self.layout.addWidget(self.margin_label, 10, 1 )
        self.layout.addWidget(self.margin_box, 10, 2 )

        self.layout.addWidget(self.add_button, 11, 3)

    def handleAddButton(self):


        if self.sex_cb.isChecked() and self.race_cb.isChecked() and self.income_cb.isChecked():
            if self.causal_cb.isChecked() and not self.group_cb.isChecked():
                self.all_measurements.append("Discrimination Search Causal")
                self.all_thresholds.append(self.threshold_box.text())
            elif self.group_cb.isChecked() and not self.causal_cb.isChecked():
                self.all_measurements.append("Discrimination Search Group")
                self.all_thresholds.append(self.threshold_box.text())
            else:
                self.all_measurements.append("Discrimination Search ")
                self.all_thresholds.append(self.threshold_box.text())
        else:

            if self.group_cb.isChecked():
                self.all_measurements.append(self.group_cb.text())        
                print(self.group_cb.text())
            elif self.causal_cb.isChecked():
                self.all_measurements.append(self.causal_cb.text())
                print (self.causal_cb.text())           
            

        self.all_confs.append(self.conf_box.text())
        self.all_margins.append(self.margin_box.text())

        inputs = []
        if self.sex_cb.isChecked():
            inputs.append("sex")
        if self.race_cb.isChecked():
            inputs.append("race")
        if self.income_cb.isChecked():
            inputs.append("income")

        inputs_tuple = tuple(inputs)

        self.all_inputs.append(inputs_tuple)

        self.accept()

        
            
    def handleDoneButton(self):
        self.accept()


    
class EditInputWindow(QDialog):

    def __init__(self, add=False, name=None, kind="categorical", values=None):
        super().__init__()
        if add == False:
            self.title = "Edit Input"
        else:
            self.title = 'Add Inputs'
        self.left = 100
        self.top = 100
        self.width = 500
        self.height = 300

        if name == None and kind == "categorical" and values == None: 
            self.initUI()
        else:
            self.initUI(name, kind, values)


    def initUI(self, name=None, kind="categorical",values=None):
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)

        if name == None and kind == "categorical" and values == None:
            self.createGrid()
        else:
            self.createGrid(name, kind, values)

        windowLayout = QVBoxLayout()
        windowLayout.addWidget(self.horizontalGroupBox)
        self.setLayout(windowLayout)

    def createGrid(self, name=None, kind="categorical", values=None):
        self.horizontalGroupBox = QGroupBox("")
        layout = QGridLayout()

        self.all_names = []
        self.all_values = []
        self.all_types = []

        name_label = QLabel("Input name: ")
        self.name_box = QLineEdit(self)

        if name is not None:
            self.name_box.setText(str(name))

        layout.addWidget(name_label, 1, 1)
        layout.addWidget(self.name_box, 1, 2)

        self.type_label = QLabel("Input type: ")
        self.types = QComboBox()
        self.types.addItem("Categorical")
        self.types.addItem("Continuous Int")

        if kind == "continuousInt":
            index = self.types.findText("Continuous Int")
            if index >= 0:
                self.types.setCurrentIndex(index)
            else:
                print ("Can't find item in combo box!")
        

        layout.addWidget(self.type_label, 2, 1)
        layout.addWidget(self.types, 2, 2)
        self.values_label = QLabel("Values (separated by commas): ")
        self.values_box = QLineEdit(self)

        if values is not None:
            self.values_box.setText(values)
            
            
        layout.addWidget(self.values_label, 3, 1)
        layout.addWidget(self.values_box, 3, 2)
        
        self.types.currentIndexChanged.connect(self.selectionChange)

        self.add_button = QPushButton("Add")
        self.add_button.setAutoDefault(False)
        self.add_button.clicked.connect(self.handleAddButton)
        layout.addWidget(self.add_button, 4,4)

        
        self.horizontalGroupBox.setLayout(layout)

    def selectionChange(self):

        if self.types.currentText() == "Continuous Int":
            self.values_label.setText("Range (e.g. 1-10) : ")
        else:
            self.values_label.setText("Values (separated by commas): ")

    def handleAddButton(self):
        
        self.all_names.append(self.name_box.text())
        self.all_values.append(self.values_box.text())
        self.all_types.append(self.types.currentText())

        self.accept()


    def handleDoneButton(self):
        
        self.accept()


if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = App()
    sys.exit(app.exec_())
