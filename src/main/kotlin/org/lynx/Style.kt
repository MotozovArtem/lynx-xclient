package org.lynx

import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import tornadofx.*

class Style : Stylesheet() {
    companion object {
        val lynxForm by cssclass()
        val formInput by cssclass()
        val warningLabel by cssclass()
        val successLabel by cssclass()
        val header by cssclass()
        val list by cssclass()
        val messageList by cssclass()
        val messageBox by cssclass()
        val messageText by cssclass()
        val ownerMessage by cssclass()
        val otherMessage by cssclass()
        val fileLink by cssclass()
        val roundedLeftButton by cssclass()
        val roundedRightButton by cssclass()
        val boldLabel by cssclass()
        val italicLabel by cssclass()
        val redButton by cssclass()
        val settingsChoiceBox by cssclass()
        val darkBackground by cssclass()
        val lightRounded by cssclass()


        // Roboto Font
        val robotoFontRegular = Font.loadFont(
            Style::class.java.classLoader.getResource("fonts/Roboto-Regular.ttf")!!.toExternalForm(),
            12.0
        )!!
        val robotoFontBold =
            Font.loadFont(Style::class.java.classLoader.getResource("fonts/Roboto-Bold.ttf")!!.toExternalForm(), 12.0)!!
        val robotoFontBlack = Font.loadFont(
            Style::class.java.classLoader.getResource("fonts/Roboto-Black.ttf")!!.toExternalForm(),
            12.0
        )!!
        val robotoFontItalic = Font.loadFont(
            Style::class.java.classLoader.getResource("fonts/Roboto-Italic.ttf")!!.toExternalForm(),
            12.0
        )!!
        val robotoFontBoldItalic = Font.loadFont(
            Style::class.java.classLoader.getResource("fonts/Roboto-BoldItalic.ttf")!!.toExternalForm(),
            12.0
        )!!
        val robotoFontBlackItalic = Font.loadFont(
            Style::class.java.classLoader.getResource("fonts/Roboto-BlackItalic.ttf")!!.toExternalForm(),
            12.0
        )!!

        val blueStop = listOf(Stop(0.0, c("#1D53FC")), Stop(1.0, c("#092F9A")))
        val blueGradient = LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, blueStop)
        val redStop = listOf(Stop(0.0, c("#F04343")), Stop(1.0, c("#8B2222")))
        val redGradient = LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE, redStop)
    }

    init {
        root {
            font = robotoFontRegular
            fontSize = 14.pt
            backgroundColor += c("#2F3540")
        }
        listView {
            backgroundColor += c("#20242C")
            borderColor += box(c("#20242C"))
            focusTraversable = false
            listCell and selected {
                backgroundColor += c("#092F9A")
            }
            scrollBar and vertical {
                scaleX = 0
                prefWidth = 0.px
            }
        }
        messageList {
            listView {
                backgroundColor += c("#262B34")
            }
            listCell and odd {
                backgroundColor += c("#262B34")
            }
            listCell and even {
                backgroundColor += c("#262B34")
            }
        }
        messageText {
            wrapText = true
        }
        listCell and odd {
            backgroundColor += c("#20242C")
        }
        listCell and even {
            backgroundColor += c("#20242C")
        }
        label {
            textFill = Color.WHITE
        }
        boldLabel {
            fontWeight = FontWeight.BOLD
        }
        italicLabel {
            font = robotoFontItalic
        }
        successLabel {
            textFill = c("#76CF74")
        }
        warningLabel {
            textFill = c("#E99F5A")
        }
        lynxForm {
            backgroundColor += c("#262B34")
            backgroundRadius = multi(box(10.px))
        }
        formInput {
            textFill = Color.WHITE
            backgroundColor += c("#2F3540")
        }
        header {
            backgroundColor += c("#1E2127")
        }
        button {
            fontSize = 20.px
            backgroundColor += blueGradient
            textFill = Color.WHITE
        }
        redButton {
            fontSize = 20.px
            backgroundColor += redGradient
            textFill = Color.WHITE
        }
        roundedLeftButton {
            borderRadius = multi(box(0.px, 10.px))
        }
        roundedRightButton {
            borderRadius = multi(box(0.px, 10.px))
        }
        messageBox {
            textFill = Color.WHITE
            backgroundColor += c("#262B34")
            wrapText = true
            content {
                backgroundColor += c("#262B34")
            }
        }
        fileLink {
            textFill = c("#7F37ED")
        }
        ownerMessage {
            backgroundColor += c("#20242C")
            backgroundRadius = multi(box(17.px))
        }
        otherMessage {
            backgroundColor += c("#20242C")
            backgroundRadius = multi(box(17.px))
        }
        settingsChoiceBox {
            backgroundColor += c("#20242C")
            backgroundRadius = multi(box(17.px))
            openButton {
                backgroundColor += c("#20242C")
                backgroundRadius = multi(box(17.px))
                arrow {
                    backgroundRadius = multi(box(17.px))
                    backgroundColor += Color.WHITE
                }
            }
        }
        lightRounded {
            borderRadius = multi(box(10.px))
            backgroundRadius = multi(box(10.px))
        }
        darkBackground {
            backgroundColor += c("#20242C")
        }
    }
}