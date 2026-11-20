#include <linux/module.h>
#include <linux/moduleparam.h>

// parametr podawany przy ładowaniu: insmod kurs.ko imie=Jan
static char *imie = "Yocto";
module_param(imie, charp, 0444);
MODULE_PARM_DESC(imie, "imie do powitania");

static int __init kurs_init(void)
{
	pr_info("kurs: witaj, %s!\n", imie);
	return 0;
}

static void __exit kurs_exit(void)
{
	pr_info("kurs: do widzenia, %s!\n", imie);
}

module_init(kurs_init);
module_exit(kurs_exit);

MODULE_LICENSE("GPL");
MODULE_DESCRIPTION("Prosty modul jadra - kurs Yocto");
