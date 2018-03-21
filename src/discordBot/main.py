#/usr/bin/env python3
#This is the discord bot bitquest
#it require python3.6 and discord.py lib

import discord
from discord.ext.commands import Bot
import asyncio
import os

'''exit code :
0	| unknow error
1	| exited normaly
2	| the file key.txt deosn't contain any key, or the format isn't good.
'''

client = Bot(command_prefix="$")

@client.event
async def on_ready():
	print('Logged in as ' + client.user.name + ' ' + client.user.id)

@client.command(pass_context = True)
async def ping(e):
	"""pong"""
	print(str(e.message.author.name) + " make a ping")
	await client.send_message(e.message.channel, "pong")

if not os.path.exists("key.txt"):
	print("no file key.txt, creating")
	f = open("key.txt","w")
	f.close()
	print("key.txt created, please the discord bot key here (add the key on the first line)")
	os.exit(2)

f = open("key.txt","r")
r = f.read().split("\n")[0] #keeping only the first line (cause some kernel and editor add an empty line at the end of some file)
f.close()
if len(r) <= 0:
	print("The first line of key.txt is empty, please add it here your discord key.")
	os.exit(2)
client.run(r)
