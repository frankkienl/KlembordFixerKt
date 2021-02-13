package nl.frankkie.klembordfixer

import java.awt.*
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun main() {
    Main()
}

class Main {

    val defaultFolder = ""
    var selectedFolder = defaultFolder

    init {
        startTray()
    }

    fun startTray() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            println("SystemTray is not supported")
            JOptionPane.showMessageDialog(null, "Error, SystemTray not supported")
            return
        }
        val popup = PopupMenu()
        val trayIcon = TrayIcon(createImage("icon.png", "tray icon"))
        val tray = SystemTray.getSystemTray()

        // Create a pop-up menu components
        val pickFolderItem = MenuItem("Pick Folder")
        val copyNowItem = MenuItem("Copy now")
        val aboutItem = MenuItem("About")
        val exitItem = MenuItem("Exit")

        //Add components to pop-up menu
        popup.add(pickFolderItem)
        popup.add(copyNowItem)
        popup.addSeparator()
        popup.add(aboutItem)
        popup.addSeparator()
        popup.add(exitItem)

        //Add action listeners
        pickFolderItem.addActionListener { pickFolder() }
        copyNowItem.addActionListener { copyNow() }
        aboutItem.addActionListener { about() }
        exitItem.addActionListener { exitApp() }

        trayIcon.popupMenu = popup
        try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added.")
        }
    }

    fun pickFolder() {
        val fc = JFileChooser()
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val isApproved = fc.showOpenDialog(null)
        if (isApproved == JFileChooser.APPROVE_OPTION) {
            val selectedFolderFile = fc.selectedFile
            selectedFolder = selectedFolderFile.absolutePath
            JOptionPane.showMessageDialog(null, "selected folder:\n$selectedFolder")
        }
    }

    fun copyNow() {
        val folderFile = File(selectedFolder)
        //get files
        val filesList = arrayListOf<File>()
        for (file in folderFile.listFiles()) {
            //filter out folders
            if (file.isDirectory) {
                continue
            }
            if (!file.canRead()) {
                continue
            }
            if (!isCorrectType(file)) {
                continue
            }
            filesList.add(file)
        }
        //sort by name
        filesList.sortBy { file -> file.name }
        //read files
        val sb = StringBuilder()
        for (file in filesList) {
            println("file found: ${file.name}")
            val fileString = file.readText()
            sb.append(fileString)
            sb.append("\n")
        }
        //Set to clipboard
        val stringSelection = StringSelection(sb.toString())
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        try {
            clipboard.setContents(stringSelection, null)
            JOptionPane.showMessageDialog(null, "Copied to clipboard")
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(null, "Error: Cannot access clipboard")
        }
    }

    /**
     * Check file extentions
     */
    fun isCorrectType(file: File): Boolean {
        val correctTypes = arrayOf("sqf", "txt", "SQF")
        if (file.name.length < 5) { return false }
        val ext = file.name.subSequence(file.name.length - 3, file.name.length)
        return correctTypes.contains(ext)
    }

    fun about() {
        JOptionPane.showMessageDialog(
            null,
            "Get all text files from a folder, copy to clipboard; Ask FrankkieNL for details"
        )
    }

    fun exitApp() {
        println("Exit app")
        exitProcess(0)
    }

    fun createImage(path: String, description: String?): Image? {
        //https://docs.oracle.com/javase/tutorial/uiswing/misc/systemtray.html
        val imageURL: URL = this::class.java.classLoader.getResource(path)
        //val imageURL = URL("file://C:\\icon.png")
        return ImageIcon(imageURL, description).image
    }
}