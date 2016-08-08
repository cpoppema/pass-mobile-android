# pass-mobile-android

pass-mobile-android is a mobile app for android your secrets managed by `pass` to your mobile device. Read all about `pass` [here](http://www.passwordstore.org).

## How to use

If you have not installed a compatible server yet, you can just install one:

* [node-server](https://github.com/cpoppema/pass-server-node#installation) for a server written in JavaScript

For this setup to work, secrets must be stored in the format `url/username`, for example: `github.com/cpoppema`. You can still categorize secrets since the server assumes the filename is the username and the directory the secrets exists in is the url. Multiline secrets are no issue either since the app can display lines and copies only the first line of your secret as the password.

## How to install

Install the app by either:
- adding it from the [play store](https://play.google.com/store/apps/details?id=mobile.android.pass) (this shows some screenshots also) ([beta access](https://play.google.com/apps/testing/mobile.android.pass))
- adding it from the play store from your device by searching for `pass mobile`
- building it yourself
    * clone this repository
    * open it in [android-studio](https://developer.android.com/studio/install.html)
    * install a build onto your connected device

## How to give this extension access to your passwords

Since your secrets are decrypted directly on your mobile device, you will need to generate a keypair and import the public key on your server, this is easy.

After installing the app, follow the instructions and open the settings view. There you'll find a form that helps you create a keypair and save it on your device. After generating it you can copy the public key from your settings and what is left is to import the public key by connecting with your server and run these commands.

The only things required are that you already have `gpg` and `pass` installed.

Replace this sample key with the key that you just generated in the settings.

Run:
```Shell
echo "-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: BCPG v@RELEASE_NAME@

mQENBFeEyGMBCAC6OeQK3uU8WqX4Wmd/LvkFpvX63R2OFljfX7EVbEo2jApZHLpd
l90ehayqRgFTPjAxOirA+E/mhtpbWB0awMcrVd3Fqa3lw9UgzlaFgpyEbnwmti2R
+zol+xlh0Fwg9cEPv/2wAuVqQJxzEjQpfhnKX+fSe+reqoKAb1WFkRTh/xOVfxhR
9gHvQ5R6c9kcUgn0csigXIiTOsenwRAfy0b+lPJ99kDrwS4PxNou9PQEFs0YFkhA
7zUd9V9pE+9q3C3h3ZooGnof9+4E7VqxoRohALXLW7fR3H+ngtCytUQL809+BRfr
h5R4avmf317ht1DqKkwJc4wpLQJ8xuKfVCDJABEBAAG0B25leHVzLTWJARwEEAEC
AAYFAleEyGMACgkQLNpAYnFCqHl9CAgAkZ9GDScr9uhq5x64wzcmT8k56mhCrdvz
sxcj9lu3rLMUmKVznRYCkY9iQlej8fxmKUz8bILfQWwqFi2EBuySZl9W5ztOCfnR
pmRJPteEEhZbPPPkeYkLvdZ/c2urn9CBmtM/nmBjiq+j6jRGbAKGClORbtxU7Z/R
rstSHDCfghG+olY1tdOtAMhU/x8jK7kn6jgSCIxUAiEFQtANXnvgXNe7bHK91WFC
xIReOKKETj/HYFQSHU3D3/dNYBCE9r3i7awgEoUJk48Jpn8mDmsz9YaF8CgxwW4B
KKSxXwgCRMGAcR9LmHQ7RUIg+10d1L9U6zlIxvfzm51JWXKhG7NJ2w==
=9Cfk
-----END PGP PUBLIC KEY BLOCK-----" | gpg --import -
```

You will see something like this, all you need to remember is one thing from this output: the key ID. The ID here is `7142A879`. You can also use the longer key ID you can find in the settings. In this example it would be 2CDA40627142A879.

```Shell
gpg: /home/me/.gnupg/trustdb.gpg: trustdb created
gpg: key 7142A879: public key "nexus-5" imported
gpg: Total number processed: 1
gpg:               imported: 1  (RSA: 1)
```

All that is left is to:

* sign your imported key
* tell `pass` to allow this key to access your secrets

**Sign your imported key**

To tell `gpg` you trust this source you're required to sign this key before it is of any use.

Run
```Shell
gpg --sign-key 7142A879
```

Signing requires a trusted key on your server. A trusted key is also required for `pass` to add new secrets.

**Tell pass to allow this key**

All that is left is to tell `pass` to encrypt your secrets using this public key so the mobile app can decrypt it on your mobile device.

```Shell
pass init {KEY ID 1} {KEY ID 2}
```

So let's say your existing trusted key's ID is AF3D26E5, run
```Shell
pass init AF3D26E5 7142A879
```

The output might look something like this if you've only setup `pass` just now:

```Shell
mkdir: created directory ‘/home/me/.password-store/’
Password store initialized for AF3D26E5, 7142A879
```

Or if you already had a password store, it probably looks more like this:

```Shell
Password store initialized for AF3D26E5, 7142A879
github.com/cpoppema: reencrypting to AF3D26E5 7142A879
```

**Credits**
* [Marco](https://github.com/m-vellinga) for kickstarting this android project with me.
* [Jason](http://www.zx2c4.com/) for writing `pass`
