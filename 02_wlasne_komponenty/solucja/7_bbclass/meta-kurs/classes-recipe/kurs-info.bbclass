# kurs-info: plik /usr/share/kurs/<nazwa recepty> z informacjami o recepcie

# nowy task w Pythonie: zapisuje informacje do pliku w katalogu buildu
python do_kurs_info() {
    info = 'nazwa: ' + d.getVar('PN') + '\n'
    info += 'wersja: ' + d.getVar('PV') + '\n'
    info += 'licencja: ' + d.getVar('LICENSE') + '\n'
    # opis z recepty (bez SUMMARY bitbake.conf daje "<PN> version <PV>-<PR>")
    info += 'opis: ' + d.getVar('SUMMARY') + '\n'
    # nazwa warstwy z BBFILE_COLLECTIONS, ustawiana przez bitbake przy parsowaniu recepty
    info += 'warstwa: ' + d.getVar('FILE_LAYERNAME') + '\n'
    bb.note('kurs-info:\n' + info)
    with open(d.getVar('B') + '/kurs-info', 'w') as f:
        f.write(info)
}
addtask kurs_info after do_compile before do_install
do_kurs_info[doc] = "Zapisuje nazwę, wersję i licencję do pliku kurs-info"

# do_install z recepty (albo z innej klasy) + nasza linia na końcu
do_install:append() {
    install -D -m 0644 ${B}/kurs-info ${D}${datadir}/kurs/${PN}
}

# /usr/share/kurs nie ma w domyślnych FILES paczki
# :append, nie += - klasa module ustawia FILES:${PN} = "" (kolejność inherit nie ma znaczenia)
FILES:${PN}:append = " ${datadir}/kurs"
