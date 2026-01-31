package ui.component

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import util.StyleConstants

class HelpDialog : Dialog<Void?>() {

    init {
        title = "Hulp - Foto Categoriseerder"
        headerText = "Gebruikershandleiding"

        dialogPane.content = createContent()
        dialogPane.buttonTypes.add(ButtonType.OK)

        dialogPane.style = """
            -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
            -fx-font-family: ${StyleConstants.FONT_FAMILY};
        """.trimIndent()

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.text = "Sluiten"
        okButton.style = buildPrimaryButtonStyle()

        dialogPane.prefWidth = 600.0
        dialogPane.prefHeight = 550.0

        setResultConverter { null }
    }

    private fun createContent(): ScrollPane {
        val contentBox = VBox().apply {
            spacing = StyleConstants.SPACING_LG
            padding = Insets(StyleConstants.SPACING_BASE)
            style = "-fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};"
        }

        contentBox.children.addAll(
            createWelcomeSection(),
            createSection("1. Foto's Uploaden", """
                • Klik op de knop "Upload Zip" in de bovenste werkbalk
                • Selecteer een ZIP-bestand dat uw foto's bevat
                • Ondersteunde formaten: JPG, JPEG, PNG, GIF, BMP
                • Alle foto's worden automatisch geladen en weergegeven in het hoofdrooster
            """.trimIndent()),
            
            createSection("2. Foto Weergave", """
                • Alle niet-gecategoriseerde foto's verschijnen in het linker paneel
                • Scroll verticaal om alle foto's te bekijken
                • Het rooster past zich automatisch aan op basis van de venstergrootte
                • Foto's behouden hun oorspronkelijke volgorde uit het ZIP-bestand
            """.trimIndent()),
            
            createSection("3. Categorieën Maken", """
                • Klik op "Add Category" in het rechter paneel
                • Kies een startnummer voor de eerste categorie
                • Kies hoeveel categorieën u wilt aanmaken
                • Categorieën worden automatisch genummerd (bijv. 1, 2, 3, ...)
                • Het categorienummer wordt gebruikt bij het exporteren
            """.trimIndent()),
            
            createSection("4. Foto's Selecteren", """
                • Klik: Selecteer één foto (deselecteert alle andere)
                • Ctrl + Klik: Voeg foto toe aan of verwijder uit huidige selectie
                • Shift + Klik: Selecteer alle foto's tussen de laatste klik en deze klik
                • Geselecteerde foto's worden gemarkeerd met een blauwe rand
            """.trimIndent()),
            
            createSection("5. Foto's Organiseren (Drag & Drop)", """
                • Selecteer één of meerdere foto's
                • Sleep de geselecteerde foto's naar een categorie in het rechter paneel
                • Laat los om de foto's aan de categorie toe te voegen
                • Het aantal foto's in de categorie wordt automatisch bijgewerkt
                • U kunt foto's ook van de ene categorie naar de andere slepen
            """.trimIndent()),
            
            createSection("6. Categorie Bekijken en Bewerken", """
                • Klik op het oog-icoon (◉) op een categorie-kaart
                • De foto's van die categorie worden getoond in het linker paneel
                • Sleep foto's binnen de categorie om de volgorde te wijzigen
                • Klik op het verwijder-icoon (×) op een foto om deze uit de categorie te halen
                • Verwijderde foto's keren terug naar de hoofdverzameling
            """.trimIndent()),
            
            createSection("7. Categorie Verwijderen", """
                • Klik op het verwijder-icoon (×) op de categorie-kaart
                • Bevestig de verwijdering in het dialoogvenster
                • Alle foto's in de categorie keren terug naar de hoofdverzameling
                • De foto's behouden hun oorspronkelijke volgorde
                • De categorie wordt permanent verwijderd
            """.trimIndent()),
            
            createSection("8. Foto's Roteren", """
                • Beweeg de muis over een foto om de rotatie-knoppen te zien
                • Klik op ↺ om de foto 90° naar links te draaien
                • Klik op ↻ om de foto 90° naar rechts te draaien
                • De rotatie wordt toegepast bij het exporteren
            """.trimIndent()),
            
            createSection("9. Exporteren", """
                • Klik op "Export" in de bovenste werkbalk
                • Kies een bestemmingsmap voor de geëxporteerde foto's
                • Foto's worden opgeslagen met de volgende naamgeving:
                  [categorienummer]_[positie in 5 cijfers].[extensie]
                • Voorbeelden: 1_00001.jpg, 1_00002.jpg, 2_00001.png
                • Alleen gecategoriseerde foto's worden geëxporteerd
            """.trimIndent()),
            
            createSection("Sneltoetsen Overzicht", """
                • Klik: Selecteer één foto
                • Ctrl + Klik: Meerdere foto's selecteren/deselecteren
                • Shift + Klik: Selecteer een reeks foto's
                • Slepen: Verplaats foto's naar een categorie
            """.trimIndent()),
            
            createSection("Tips", """
                • Maak eerst alle benodigde categorieën aan voordat u begint met sorteren
                • Gebruik Shift + Klik voor het snel selecteren van opeenvolgende foto's
                • Bekijk een categorie regelmatig om de volgorde te controleren
                • Foto's kunnen altijd terug naar de hoofdverzameling worden gehaald
            """.trimIndent())
        )

        return ScrollPane(contentBox).apply {
            isFitToWidth = true
            style = """
                -fx-background: ${StyleConstants.BACKGROUND_PRIMARY};
                -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
            """.trimIndent()
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        }
    }

    private fun createWelcomeSection(): VBox {
        return VBox().apply {
            spacing = StyleConstants.SPACING_SM
            children.addAll(
                Label("Welkom bij Foto Categoriseerder").apply {
                    style = """
                        -fx-font-size: ${StyleConstants.FONT_SIZE_XL}px;
                        -fx-font-weight: bold;
                        -fx-text-fill: ${StyleConstants.PRIMARY_600};
                    """.trimIndent()
                },
                Label("Deze applicatie helpt u bij het organiseren van foto's in categorieën via drag-and-drop. " +
                      "Hieronder vindt u een complete handleiding voor alle functies.").apply {
                    style = """
                        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                        -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
                        -fx-wrap-text: true;
                    """.trimIndent()
                    isWrapText = true
                }
            )
        }
    }

    private fun createSection(title: String, content: String): VBox {
        return VBox().apply {
            spacing = StyleConstants.SPACING_SM
            padding = Insets(0.0, 0.0, StyleConstants.SPACING_SM, 0.0)
            
            children.addAll(
                Label(title).apply {
                    style = """
                        -fx-font-size: ${StyleConstants.FONT_SIZE_MD}px;
                        -fx-font-weight: 600;
                        -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
                    """.trimIndent()
                },
                Label(content).apply {
                    style = """
                        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                        -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
                        -fx-wrap-text: true;
                        -fx-padding: 0 0 0 ${StyleConstants.SPACING_BASE};
                    """.trimIndent()
                    isWrapText = true
                    maxWidth = 550.0
                }
            )
        }
    }

    private fun buildPrimaryButtonStyle(): String = """
        -fx-background-color: ${StyleConstants.PRIMARY_500};
        -fx-text-fill: white;
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-font-weight: 600;
        -fx-padding: 10 24 10 24;
        -fx-background-radius: ${StyleConstants.RADIUS_BASE};
        -fx-cursor: hand;
    """.trimIndent()
}
