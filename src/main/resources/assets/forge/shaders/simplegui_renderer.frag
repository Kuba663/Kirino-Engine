#version 460 core

#define DRAW_RECT 1
#define DRAW_LINES 2
#define DRAW_BEZIER 3

#define FLAG_RADIUS 1
#define FLAG_BORDER 2
#define FLAG_SHADOW 4

in flat int DrawType;
in flat int Flags;
in flat vec4 Color;
in flat vec4 BorderColor;
in flat vec4 ShadowColor;
in flat vec4 Rect;
in vec2 LocalPos;
in flat float BorderWidth;
in flat float ShadowBlur;
in flat vec2 ShadowOffset;
in flat float Radius;
in flat float Pad;
in float BorderDist;

out vec4 FragColor;

void main()
{
    if (DrawType == DRAW_RECT)
    {
        bool hasRadius = (Flags & FLAG_RADIUS) != 0;
        bool hasBorder = (Flags & FLAG_BORDER) != 0;
        bool hasShadow = (Flags & FLAG_SHADOW) != 0;

        FragColor = Color;

        if (hasRadius)
        {
            if (!hasBorder && !hasShadow)
            {
                FragColor = Color;
            }
            else if (hasBorder && !hasShadow)
            {
                float aaWidth = 0.08;
                if (BorderDist < 1.0 - aaWidth)
                {
                    FragColor = Color;
                }
                else if (BorderDist > 1.0 + aaWidth)
                {
                    FragColor = BorderColor;
                }
                else
                {
                    float t = smoothstep(1.0 - aaWidth, 1.0 + aaWidth, BorderDist);
                    FragColor = mix(Color, BorderColor, t);
                }
            }
            else if (!hasBorder && hasShadow)
            {

            }
            else if (hasBorder && hasShadow)
            {

            }
        }
        else
        {
            if (!hasBorder && !hasShadow)
            {

            }
            else if (hasBorder && !hasShadow)
            {

            }
            else if (!hasBorder && hasShadow)
            {

            }
            else if (hasBorder && hasShadow)
            {

            }
        }
    }
    else if (DrawType == DRAW_LINES)
    {

    }
    else if (DrawType == DRAW_BEZIER)
    {

    }
    else
    {
        discard;
    }
}
