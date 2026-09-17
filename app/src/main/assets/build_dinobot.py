import base64,gzip,pathlib
root=pathlib.Path(__file__).parent
parts=[]
for n in (1,2): parts.append((root/f'dinobot_html_part{n}.b64').read_text().strip())
data=base64.b64decode(''.join(parts))
html=gzip.decompress(data).decode('utf-8')
(root/'index.html').write_text(html,encoding='utf-8')
print('DinoBot HTML reconstructed:',len(html),'bytes')
